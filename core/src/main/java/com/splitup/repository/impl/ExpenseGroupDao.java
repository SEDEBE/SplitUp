package com.splitup.repository.impl;

import com.splitup.model.ExpenseGroup;
import com.splitup.model.User;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * DAO para la entidad ExpenseGroup (tabla expense_groups).
 */
public class ExpenseGroupDao extends AbstractDao<ExpenseGroup, Long> {

    private static final Logger log = LoggerFactory.getLogger(ExpenseGroupDao.class);

    public ExpenseGroupDao() {
        super(ExpenseGroup.class);
    }

    /**
     * Devuelve todos los grupos en los que participa un usuario.
     * Hace JOIN con group_members para encontrarlos.
     *
     * @param user Usuario del que se quieren los grupos
     * @return Lista de grupos
     */
    public List<ExpenseGroup> findByMember(User user) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "SELECT gm.group FROM GroupMember gm " +
                            "WHERE gm.user = :user " +
                            "ORDER BY gm.group.createdAt DESC",
                    ExpenseGroup.class)
                    .setParameter("user", user)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByMember(userId={}): {}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al obtener grupos del usuario", e);
        }
    }

    /**
     * Devuelve todos los grupos creados por un usuario concreto.
     *
     * @param owner Usuario creador
     * @return Lista de grupos
     */
    public List<ExpenseGroup> findByOwner(User owner) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM ExpenseGroup g WHERE g.owner = :owner ORDER BY g.createdAt DESC",
                    ExpenseGroup.class)
                    .setParameter("owner", owner)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByOwner(userId={}): {}", owner.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al obtener grupos por propietario", e);
        }
    }

    /**
     * Busca grupos cuyo nombre contenga el texto dado.
     *
     * @param namePart Texto parcial (case-insensitive)
     * @return Lista de grupos
     */
    public List<ExpenseGroup> findByNameContaining(String namePart) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM ExpenseGroup g WHERE LOWER(g.name) LIKE :pattern ORDER BY g.name",
                    ExpenseGroup.class)
                    .setParameter("pattern", "%" + namePart.trim().toLowerCase() + "%")
                    .list();
        } catch (Exception e) {
            log.error("Error en findByNameContaining({}): {}", namePart, e.getMessage(), e);
            throw new RuntimeException("Error al buscar grupos por nombre", e);
        }
    }
}