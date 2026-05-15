package com.splitup.repository.impl;

import com.splitup.model.ExpenseGroup;
import com.splitup.model.GroupMember;
import com.splitup.model.User;
import com.splitup.model.enums.GroupRole;
import com.splitup.model.ids.GroupMemberId;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * DAO para la entidad GroupMember.
 * Usa clave primaria compuesta: GroupMemberId (groupId + userId).
 */
public class GroupMemberDao extends AbstractDao<GroupMember, GroupMemberId> {

    private static final Logger log = LoggerFactory.getLogger(GroupMemberDao.class);

    public GroupMemberDao() {
        super(GroupMember.class);
    }

    /**
     * Devuelve todos los miembros de un grupo.
     *
     * @param group Grupo
     * @return Lista de GroupMember
     */
    public List<GroupMember> findByGroup(ExpenseGroup group) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM GroupMember gm WHERE gm.group = :group ORDER BY gm.joinedAt",
                    GroupMember.class)
                    .setParameter("group", group)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByGroup(groupId={}): {}", group.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al obtener miembros del grupo", e);
        }
    }

    /**
     * Busca la relación concreta entre un usuario y un grupo.
     *
     * @param user  Usuario
     * @param group Grupo
     * @return Optional con el GroupMember, o vacío si no pertenece al grupo
     */
    public Optional<GroupMember> findByUserAndGroup(User user, ExpenseGroup group) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM GroupMember gm WHERE gm.user = :user AND gm.group = :group",
                    GroupMember.class)
                    .setParameter("user", user)
                    .setParameter("group", group)
                    .uniqueResultOptional();
        } catch (Exception e) {
            log.error("Error en findByUserAndGroup(): {}", e.getMessage(), e);
            throw new RuntimeException("Error al buscar miembro en grupo", e);
        }
    }

    /**
     * Comprueba si un usuario pertenece a un grupo.
     *
     * @param user  Usuario
     * @param group Grupo
     * @return true si es miembro
     */
    public boolean isMember(User user, ExpenseGroup group) {
        return findByUserAndGroup(user, group).isPresent();
    }

    /**
     * Devuelve los miembros de un grupo con un rol concreto.
     *
     * @param group Grupo
     * @param role  Rol a filtrar (ADMIN o MEMBER)
     * @return Lista de GroupMember
     */
    public List<GroupMember> findByGroupAndRole(ExpenseGroup group, GroupRole role) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM GroupMember gm WHERE gm.group = :group AND gm.role = :role",
                    GroupMember.class)
                    .setParameter("group", group)
                    .setParameter("role", role)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByGroupAndRole(): {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener miembros por rol", e);
        }
    }

    /**
     * Igual que findByGroup pero con JOIN FETCH sobre user y group para que
     * los servicios puedan acceder a esas relaciones una vez cerrada la sesión.
     *
     * @param group Grupo
     * @return Lista de GroupMember con user y group ya inicializados
     */
    public List<GroupMember> findByGroupFetchingUsers(ExpenseGroup group) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM GroupMember gm JOIN FETCH gm.user JOIN FETCH gm.group " +
                    "WHERE gm.group = :group ORDER BY gm.joinedAt",
                    GroupMember.class)
                    .setParameter("group", group)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByGroupFetchingUsers(groupId={}): {}", group.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al obtener miembros del grupo con usuarios", e);
        }
    }

    /**
     * Cuenta cuántos miembros tiene un grupo.
     *
     * @param group Grupo
     * @return Número de miembros
     */
    public long countByGroup(ExpenseGroup group) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group = :group",
                    Long.class)
                    .setParameter("group", group)
                    .uniqueResult();
        } catch (Exception e) {
            log.error("Error en countByGroup(groupId={}): {}", group.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al contar miembros del grupo", e);
        }
    }
}