package com.splitup.service;

import com.splitup.model.ExpenseGroup;
import com.splitup.model.GroupMember;
import com.splitup.model.User;
import com.splitup.model.enums.GroupRole;
import com.splitup.repository.impl.ExpenseGroupDao;
import com.splitup.repository.impl.GroupMemberDao;
import com.splitup.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de dominio para la gestión de grupos y su membresía.
 *
 * Invariantes de negocio que garantiza este servicio:
 *  - Un grupo siempre tiene al menos un ADMIN u OWNER.
 *  - Solo ADMIN u OWNER pueden añadir/eliminar miembros o cambiar roles.
 *  - La creación de grupo + alta del creador como OWNER es atómica (una sola transacción Hibernate).
 */
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final ExpenseGroupDao groupDao;
    private final GroupMemberDao memberDao;

    public GroupService() {
        this.groupDao = new ExpenseGroupDao();
        this.memberDao = new GroupMemberDao();
    }

    /** Constructor para tests: permite inyectar DAOs mockeados. */
    public GroupService(ExpenseGroupDao groupDao, GroupMemberDao memberDao) {
        this.groupDao = groupDao;
        this.memberDao = memberDao;
    }

    // -----------------------------------------------------------------------
    // Creación
    // -----------------------------------------------------------------------

    /**
     * Crea un grupo y añade al creador como OWNER en una sola transacción.
     *
     * Usamos una sesión Hibernate unificada en lugar de delegar en dos DAOs
     * separados porque ambas operaciones deben ser atómicas: si el alta del
     * OWNER fallase, el grupo quedaría huérfano sin ningún administrador.
     *
     * @param name        Nombre del grupo
     * @param description Descripción libre (puede ser null)
     * @param creator     Usuario que crea el grupo (pasará a ser OWNER)
     * @return ExpenseGroup persistido con ID asignado
     */
    public ExpenseGroup createGroup(String name, String description, User creator) throws BusinessException {
        if (name == null || name.isBlank())
            throw new BusinessException("El nombre del grupo no puede estar vacío");
        if (creator == null)
            throw new BusinessException("El creador no puede ser null");

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            // merge() para obtener una referencia gestionada por esta sesión
            User managedCreator = session.merge(creator);

            ExpenseGroup group = new ExpenseGroup(name.trim(), description, managedCreator);
            session.persist(group); // IDENTITY → id asignado inmediatamente tras el INSERT

            GroupMember owner = new GroupMember(managedCreator, group, GroupRole.OWNER);
            session.persist(owner);

            tx.commit();
            log.info("Grupo creado: id={}, owner=userId:{}", group.getId(), creator.getId());
            return group;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("Error al crear grupo: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear el grupo", e);
        } finally {
            session.close();
        }
    }

    // -----------------------------------------------------------------------
    // Gestión de membresía
    // -----------------------------------------------------------------------

    /**
     * Añade un nuevo miembro al grupo.
     * Solo puede hacerlo un ADMIN u OWNER.
     *
     * @param group     Grupo destino
     * @param user      Usuario a añadir
     * @param role      Rol asignado (null → MEMBER por defecto)
     * @param requester Usuario que realiza la operación
     * @return GroupMember creado
     */
    public GroupMember addMember(ExpenseGroup group, User user, GroupRole role, User requester)
            throws BusinessException {
        requireAdminOrOwner(group, requester);

        if (memberDao.isMember(user, group))
            throw new BusinessException("El usuario '" + user.getName() + "' ya es miembro del grupo");

        GroupMember member = new GroupMember(user, group, role != null ? role : GroupRole.MEMBER);
        memberDao.save(member);
        log.info("Miembro añadido: userId={} en groupId={} con rol={}", user.getId(), group.getId(), member.getRole());
        return member;
    }

    /**
     * Elimina a un miembro del grupo.
     * No se puede eliminar al último ADMIN/OWNER: el grupo quedaría sin gobierno.
     *
     * @param group     Grupo
     * @param user      Usuario a eliminar
     * @param requester Usuario que realiza la operación
     */
    public void removeMember(ExpenseGroup group, User user, User requester) throws BusinessException {
        requireAdminOrOwner(group, requester);

        GroupMember member = memberDao.findByUserAndGroup(user, group)
                .orElseThrow(() -> new BusinessException("El usuario no es miembro del grupo"));

        if (isLastAdmin(group, member))
            throw new BusinessException("No se puede eliminar al único administrador del grupo");

        memberDao.delete(member);
        log.info("Miembro eliminado: userId={} de groupId={}", user.getId(), group.getId());
    }

    /**
     * Cambia el rol de un miembro del grupo.
     * No se puede rebajar al único ADMIN/OWNER a MEMBER.
     *
     * @param group     Grupo
     * @param user      Usuario cuyo rol cambia
     * @param newRole   Nuevo rol
     * @param requester Usuario que realiza la operación
     */
    public void changeRole(ExpenseGroup group, User user, GroupRole newRole, User requester)
            throws BusinessException {
        requireAdminOrOwner(group, requester);

        GroupMember member = memberDao.findByUserAndGroup(user, group)
                .orElseThrow(() -> new BusinessException("El usuario no es miembro del grupo"));

        GroupRole oldRole = member.getRole();

        // Si este miembro era el único ADMIN/OWNER y lo bajamos a MEMBER, el grupo se quedaría sin gobierno
        if ((oldRole == GroupRole.ADMIN || oldRole == GroupRole.OWNER) && newRole == GroupRole.MEMBER
                && isLastAdmin(group, member)) {
            throw new BusinessException("No se puede rebajar al único administrador del grupo");
        }

        member.setRole(newRole);
        memberDao.update(member);
        log.info("Rol cambiado: userId={} en groupId={} de {} a {}", user.getId(), group.getId(), oldRole, newRole);
    }

    // -----------------------------------------------------------------------
    // Consultas
    // -----------------------------------------------------------------------

    public List<ExpenseGroup> getGroupsByMember(User user) {
        return groupDao.findByMember(user);
    }

    public List<GroupMember> getMembersOfGroup(ExpenseGroup group) {
        return memberDao.findByGroupFetchingUsers(group);
    }

    public Optional<GroupRole> getMemberRole(ExpenseGroup group, User user) {
        return memberDao.findByUserAndGroup(user, group).map(GroupMember::getRole);
    }

    public long getMemberCount(ExpenseGroup group) {
        return memberDao.countByGroup(group);
    }

    // -----------------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------------

    /** Comprueba que el solicitante sea ADMIN u OWNER; lanza BusinessException si no. */
    private void requireAdminOrOwner(ExpenseGroup group, User requester) throws BusinessException {
        GroupRole role = memberDao.findByUserAndGroup(requester, group)
                .map(GroupMember::getRole)
                .orElseThrow(() -> new BusinessException("El solicitante no es miembro del grupo"));

        if (role == GroupRole.MEMBER)
            throw new BusinessException("Se requiere rol ADMIN u OWNER para esta operación");
    }

    /**
     * Devuelve true si {@code member} es el único ADMIN u OWNER que queda en el grupo.
     * Se usa para proteger el invariante de que todo grupo tenga al menos un administrador.
     */
    private boolean isLastAdmin(ExpenseGroup group, GroupMember member) {
        GroupRole role = member.getRole();
        if (role != GroupRole.ADMIN && role != GroupRole.OWNER) return false;

        long adminCount = memberDao.findByGroupAndRole(group, GroupRole.ADMIN).size()
                + memberDao.findByGroupAndRole(group, GroupRole.OWNER).size();
        return adminCount <= 1;
    }
}
