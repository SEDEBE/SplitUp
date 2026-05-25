package com.splitup.repository.impl;

import com.splitup.model.Expense;
import com.splitup.model.ExpenseGroup;
import com.splitup.model.User;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DAO para la entidad Expense.
 * Contiene las consultas necesarias para el cálculo de balances.
 */
public class ExpenseDao extends AbstractDao<Expense, Long> {

    private static final Logger log = LoggerFactory.getLogger(ExpenseDao.class);

    public ExpenseDao() {
        super(Expense.class);
    }

    /**
     * Devuelve todos los gastos de un grupo, ordenados por fecha descendente.
     *
     * @param group Grupo del que se quieren los gastos
     * @return Lista de gastos
     */
    public List<Expense> findByGroup(ExpenseGroup group) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Expense e JOIN FETCH e.payer WHERE e.group = :group ORDER BY e.expenseDate DESC, e.createdAt DESC",
                    Expense.class)
                    .setParameter("group", group)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByGroup(groupId={}): {}", group.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al obtener gastos del grupo", e);
        }
    }

    /**
     * Devuelve los gastos pagados por un usuario en un grupo concreto.
     * Útil para calcular cuánto ha adelantado cada miembro.
     *
     * @param payer Usuario pagador
     * @param group Grupo
     * @return Lista de gastos
     */
    public List<Expense> findByPayerAndGroup(User payer, ExpenseGroup group) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Expense e WHERE e.payer = :payer AND e.group = :group " +
                            "ORDER BY e.expenseDate DESC",
                    Expense.class)
                    .setParameter("payer", payer)
                    .setParameter("group", group)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByPayerAndGroup(): {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener gastos por pagador y grupo", e);
        }
    }

    /**
     * Devuelve los gastos de un grupo dentro de un rango de fechas.
     *
     * @param group Grupo
     * @param from  Fecha inicio (inclusiva)
     * @param to    Fecha fin (inclusiva)
     * @return Lista de gastos
     */
    public List<Expense> findByGroupAndDateRange(ExpenseGroup group, LocalDate from, LocalDate to) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Expense e WHERE e.group = :group " +
                            "AND e.expenseDate BETWEEN :from AND :to " +
                            "ORDER BY e.expenseDate DESC",
                    Expense.class)
                    .setParameter("group", group)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByGroupAndDateRange(): {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener gastos por rango de fechas", e);
        }
    }

    /**
     * Suma el total de gastos de un grupo.
     * Devuelve 0 si no hay gastos.
     *
     * @param group Grupo
     * @return Suma total de los importes
     */
    public BigDecimal sumTotalByGroup(ExpenseGroup group) {
        try (Session session = openSession()) {
            BigDecimal result = session.createQuery(
                    "SELECT COALESCE(SUM(e.totalAmount), 0) FROM Expense e WHERE e.group = :group",
                    BigDecimal.class)
                    .setParameter("group", group)
                    .uniqueResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error en sumTotalByGroup(groupId={}): {}", group.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al sumar gastos del grupo", e);
        }
    }
}