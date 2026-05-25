package com.splitup.service;

import com.splitup.model.*;
import com.splitup.model.enums.ShareType;
import com.splitup.model.enums.SplitMode;
import com.splitup.repository.impl.ExpenseDao;
import com.splitup.repository.impl.ExpenseShareDao;
import com.splitup.repository.impl.GroupMemberDao;
import com.splitup.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio de dominio para la gestión de gastos y su reparto.
 *
 * Reglas de negocio que garantiza este servicio:
 *  - El pagador debe ser miembro del grupo.
 *  - Todos los participantes del reparto deben ser miembros del grupo.
 *  - En modo CUSTOM, la suma de importes debe ser exactamente igual al total.
 *  - La creación de un gasto y sus shares es atómica (una sola transacción Hibernate).
 *
 * Por qué sesión unificada en lugar de dos llamadas DAO:
 *  Persistir Expense y sus ExpenseShare en sesiones separadas crea una ventana
 *  de inconsistencia: si la segunda transacción falla, el gasto existe sin reparto.
 *  Al usar una sola sesión, ambas operaciones comparten la misma transacción y
 *  se revierten juntas ante cualquier error.
 */
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseDao expenseDao;
    private final ExpenseShareDao shareDao;
    private final GroupMemberDao memberDao;

    public ExpenseService() {
        this.expenseDao = new ExpenseDao();
        this.shareDao = new ExpenseShareDao();
        this.memberDao = new GroupMemberDao();
    }

    /** Constructor para tests: permite inyectar DAOs mockeados. */
    public ExpenseService(ExpenseDao expenseDao, ExpenseShareDao shareDao, GroupMemberDao memberDao) {
        this.expenseDao = expenseDao;
        this.shareDao = shareDao;
        this.memberDao = memberDao;
    }

    // -----------------------------------------------------------------------
    // Creación
    // -----------------------------------------------------------------------

    /**
     * Crea un gasto con su reparto en una sola transacción Hibernate.
     *
     * Para SplitMode.EQUAL: el importe se divide equitativamente entre los participantes.
     * Los céntimos sobrantes del redondeo se asignan al primero de la lista (habitualmente el pagador).
     *
     * Para SplitMode.CUSTOM: {@code customAmounts} debe contener un importe por cada participante
     * y la suma debe coincidir exactamente con {@code totalAmount}.
     *
     * @param group         Grupo al que pertenece el gasto
     * @param payer         Usuario que adelantó el dinero
     * @param title         Descripción del gasto (1–140 caracteres)
     * @param totalAmount   Importe total (debe ser > 0)
     * @param expenseDate   Fecha del gasto
     * @param category      Categoría (puede ser null)
     * @param note          Nota libre (puede ser null)
     * @param currency      Código de divisa, p.ej. "EUR" (null → EUR)
     * @param splitMode     EQUAL o CUSTOM (null → EQUAL)
     * @param participants  Usuarios que participan en el reparto
     * @param customAmounts Solo para CUSTOM: mapa usuario→importe asignado
     * @return Expense persistido con ID asignado
     * @throws BusinessException Si alguna regla de negocio es violada
     */
    public Expense createExpense(
            ExpenseGroup group,
            User payer,
            String title,
            BigDecimal totalAmount,
            LocalDate expenseDate,
            Category category,
            String note,
            String currency,
            SplitMode splitMode,
            List<User> participants,
            Map<User, BigDecimal> customAmounts
    ) throws BusinessException {

        validateParticipants(group, payer, participants);

        SplitMode mode = splitMode != null ? splitMode : SplitMode.EQUAL;
        List<BigDecimal> amounts = resolveAmounts(totalAmount, mode, participants, customAmounts);

        Expense expense = new Expense(group, payer, title, totalAmount, expenseDate);
        expense.setSplitMode(mode);
        if (category != null) expense.setCategory(category);
        if (note != null)     expense.setNote(note);
        if (currency != null) expense.setCurrency(currency);

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            // group y payer llegan detached; getReference() devuelve un proxy gestionado sin SELECT extra
            expense.setGroup(session.getReference(ExpenseGroup.class, group.getId()));
            expense.setPayer(session.getReference(User.class, payer.getId()));

            session.persist(expense);

            ShareType shareType = (mode == SplitMode.CUSTOM) ? ShareType.CUSTOM : ShareType.EQUAL;
            for (int i = 0; i < participants.size(); i++) {
                User managedUser = session.getReference(User.class, participants.get(i).getId());
                ExpenseShare share = new ExpenseShare(expense, managedUser, shareType, amounts.get(i));
                session.persist(share);
            }

            tx.commit();
            // Restore the original (non-proxy) references so callers can access
            // payer.getName() and group.getName() without an open session.
            expense.setPayer(payer);
            expense.setGroup(group);
            log.info("Gasto creado: id={}, groupId={}, total={}", expense.getId(), group.getId(), totalAmount);
            return expense;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("Error al crear gasto: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear el gasto", e);
        } finally {
            session.close();
        }
    }

    // -----------------------------------------------------------------------
    // Edición
    // -----------------------------------------------------------------------

    /**
     * Actualiza los datos de un gasto y recalcula su reparto de forma atómica.
     *
     * La estrategia es borrar los shares existentes con HQL bulk-delete y recrearlos.
     * Esto evita gestionar la igualdad de claves compuestas en el merge.
     * Parámetros con valor null conservan el valor actual del gasto.
     *
     * @param expense        Gasto a actualizar (entidad detached)
     * @param newTitle       Nuevo título (null → sin cambio)
     * @param newAmount      Nuevo importe (null → sin cambio)
     * @param newDate        Nueva fecha (null → sin cambio)
     * @param newCategory    Nueva categoría (null → sin categoría)
     * @param newNote        Nueva nota (null → sin cambio)
     * @param newSplitMode   Nuevo modo de reparto (null → sin cambio)
     * @param participants   Nueva lista de participantes
     * @param customAmounts  Solo para CUSTOM: mapa usuario→importe
     * @throws BusinessException Si alguna regla de negocio es violada
     */
    public void updateExpense(
            Expense expense,
            String newTitle,
            BigDecimal newAmount,
            LocalDate newDate,
            Category newCategory,
            String newNote,
            SplitMode newSplitMode,
            List<User> participants,
            Map<User, BigDecimal> customAmounts
    ) throws BusinessException {

        if (participants == null || participants.isEmpty())
            throw new BusinessException("La lista de participantes no puede estar vacía");

        ExpenseGroup group = expense.getGroup();
        for (User p : participants) {
            if (!memberDao.isMember(p, group))
                throw new BusinessException("El participante '" + p.getName() + "' no es miembro del grupo");
        }

        BigDecimal amount    = newAmount    != null ? newAmount    : expense.getTotalAmount();
        SplitMode  mode      = newSplitMode != null ? newSplitMode : expense.getSplitMode();
        List<BigDecimal> amounts = resolveAmounts(amount, mode, participants, customAmounts);

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            session.createMutationQuery("DELETE FROM ExpenseShare es WHERE es.expense = :e")
                    .setParameter("e", expense)
                    .executeUpdate();

            Expense managed = session.merge(expense);
            if (newTitle  != null) managed.setTitle(newTitle);
            if (newAmount != null) managed.setTotalAmount(newAmount);
            if (newDate   != null) managed.setExpenseDate(newDate);
            managed.setCategory(newCategory);
            if (newNote   != null) managed.setNote(newNote);
            managed.setSplitMode(mode);

            ShareType shareType = (mode == SplitMode.CUSTOM) ? ShareType.CUSTOM : ShareType.EQUAL;
            for (int i = 0; i < participants.size(); i++) {
                User managedUser = session.getReference(User.class, participants.get(i).getId());
                ExpenseShare share = new ExpenseShare(managed, managedUser, shareType, amounts.get(i));
                session.persist(share);
            }

            tx.commit();
            log.info("Gasto actualizado: id={}", expense.getId());

        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("Error al actualizar gasto id={}: {}", expense.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al actualizar el gasto", e);
        } finally {
            session.close();
        }
    }

    // -----------------------------------------------------------------------
    // Eliminación
    // -----------------------------------------------------------------------

    /**
     * Elimina un gasto y todos sus shares.
     * Los shares se borran primero para respetar la FK expense_shares → expenses.
     *
     * @param expense Gasto a eliminar
     */
    public void deleteExpense(Expense expense) {
        shareDao.deleteByExpense(expense);
        expenseDao.delete(expense);
        log.info("Gasto eliminado: id={}", expense.getId());
    }

    // -----------------------------------------------------------------------
    // Consultas
    // -----------------------------------------------------------------------

    public List<Expense> getExpensesByGroup(ExpenseGroup group) {
        return expenseDao.findByGroup(group);
    }

    public List<Expense> getExpensesByPayer(User payer, ExpenseGroup group) {
        return expenseDao.findByPayerAndGroup(payer, group);
    }

    public List<Expense> getExpensesByDateRange(ExpenseGroup group, LocalDate from, LocalDate to) {
        return expenseDao.findByGroupAndDateRange(group, from, to);
    }

    public BigDecimal getTotalByGroup(ExpenseGroup group) {
        return expenseDao.sumTotalByGroup(group);
    }

    // -----------------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------------

    /** Valida que el pagador y todos los participantes sean miembros del grupo. */
    private void validateParticipants(ExpenseGroup group, User payer, List<User> participants)
            throws BusinessException {
        if (participants == null || participants.isEmpty())
            throw new BusinessException("La lista de participantes no puede estar vacía");

        if (!memberDao.isMember(payer, group))
            throw new BusinessException("El pagador no es miembro del grupo");

        for (User p : participants) {
            if (!memberDao.isMember(p, group))
                throw new BusinessException("El participante '" + p.getName() + "' no es miembro del grupo");
        }
    }

    /**
     * Calcula la lista de importes por participante según el SplitMode.
     *
     * EQUAL: divide {@code total / n} con escala 2 redondeando hacia abajo.
     * Los céntimos sobrantes (remainder) se suman al primer participante para
     * garantizar que la suma de shares sea exactamente igual al total del gasto.
     *
     * CUSTOM: usa el mapa {@code customAmounts} y verifica que la suma sea exacta.
     */
    private List<BigDecimal> resolveAmounts(
            BigDecimal total,
            SplitMode mode,
            List<User> participants,
            Map<User, BigDecimal> customAmounts
    ) throws BusinessException {

        List<BigDecimal> result = new ArrayList<>();
        int n = participants.size();

        if (mode == SplitMode.EQUAL) {
            BigDecimal perPerson = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.DOWN);
            BigDecimal remainder = total.subtract(perPerson.multiply(BigDecimal.valueOf(n)));

            for (int i = 0; i < n; i++) {
                result.add(i == 0 ? perPerson.add(remainder) : perPerson);
            }

        } else { // CUSTOM
            if (customAmounts == null || customAmounts.isEmpty())
                throw new BusinessException("El modo CUSTOM requiere un mapa de importes por participante");

            BigDecimal sum = BigDecimal.ZERO;
            for (User p : participants) {
                BigDecimal a = customAmounts.get(p);
                if (a == null || a.signum() < 0)
                    throw new BusinessException("Importe inválido para el participante: " + p.getName());
                result.add(a);
                sum = sum.add(a);
            }

            if (sum.compareTo(total) != 0)
                throw new BusinessException(
                        "La suma de los importes personalizados (" + sum +
                        ") no coincide con el total del gasto (" + total + ")");
        }

        return result;
    }
}
