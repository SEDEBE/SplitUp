package com.splitup.repository.impl;

import com.splitup.model.Expense;
import com.splitup.model.ExpenseShare;
import com.splitup.model.User;
import com.splitup.model.ids.ExpenseShareId;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * DAO para la entidad ExpenseShare.
 * Usa clave primaria compuesta: ExpenseShareId (expenseId + userId).
 */
public class ExpenseShareDao extends AbstractDao<ExpenseShare, ExpenseShareId> {

    private static final Logger log = LoggerFactory.getLogger(ExpenseShareDao.class);

    public ExpenseShareDao() {
        super(ExpenseShare.class);
    }

    /**
     * Devuelve todos los shares de un gasto concreto.
     * Es la consulta principal para el algoritmo de balances.
     *
     * @param expense Gasto del que se quieren los shares
     * @return Lista de shares
     */
    public List<ExpenseShare> findByExpense(Expense expense) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM ExpenseShare es WHERE es.expense = :expense",
                    ExpenseShare.class)
                    .setParameter("expense", expense)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByExpense(expenseId={}): {}", expense.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al obtener shares del gasto", e);
        }
    }

    /**
     * Devuelve todos los shares en los que un usuario es deudor,
     * a lo largo de todos los gastos del sistema.
     * Útil para calcular el balance global de un usuario.
     *
     * @param user Usuario deudor
     * @return Lista de shares
     */
    public List<ExpenseShare> findByUser(User user) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM ExpenseShare es WHERE es.user = :user",
                    ExpenseShare.class)
                    .setParameter("user", user)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByUser(userId={}): {}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al obtener shares del usuario", e);
        }
    }

    /**
     * Devuelve los shares de un usuario dentro de un grupo concreto.
     * Necesario para el cálculo de balances por grupo.
     *
     * @param user    Usuario deudor
     * @param groupId ID del grupo
     * @return Lista de shares
     */
    public List<ExpenseShare> findByUserAndGroup(User user, Long groupId) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM ExpenseShare es " +
                            "WHERE es.user = :user AND es.expense.group.id = :groupId",
                    ExpenseShare.class)
                    .setParameter("user", user)
                    .setParameter("groupId", groupId)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByUserAndGroup(): {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener shares por usuario y grupo", e);
        }
    }

    /**
     * Elimina todos los shares de un gasto (útil antes de recalcular el reparto).
     *
     * @param expense Gasto cuyos shares se borran
     */
    public void deleteByExpense(Expense expense) {
        org.hibernate.Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            session.createMutationQuery(
                    "DELETE FROM ExpenseShare es WHERE es.expense = :expense")
                    .setParameter("expense", expense)
                    .executeUpdate();
            tx.commit();
            log.debug("deleteByExpense() OK: expenseId={}", expense.getId());
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            log.error("Error en deleteByExpense(expenseId={}): {}", expense.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al eliminar shares del gasto", e);
        }
    }
}