package com.splitup.repository.impl;

import com.splitup.repository.GenericDao;
import com.splitup.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementación base de GenericDao usando Hibernate.
 * Todas las operaciones CRUD quedan centralizadas aquí.
 *
 * @param <T>  Tipo de la entidad
 * @param <ID> Tipo de la clave primaria
 */
public abstract class AbstractDao<T, ID> implements GenericDao<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(AbstractDao.class);

    /**
     * Clase de la entidad concreta (ej: User.class). Necesaria para findAll y
     * findById.
     */
    private final Class<T> entityClass;

    protected AbstractDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // -----------------------------------------------------------------------
    // Acceso a sesión
    // -----------------------------------------------------------------------

    /** Abre una sesión de Hibernate. El llamador es responsable de cerrarla. */
    protected Session openSession() {
        return HibernateUtil.getSessionFactory().openSession();
    }

    // -----------------------------------------------------------------------
    // CRUD genérico
    // -----------------------------------------------------------------------

    @Override
    public void save(T entity) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
            log.debug("save() OK: {}", entity);
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            log.error("Error en save(): {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar entidad", e);
        }
    }

    @Override
    public void update(T entity) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
            log.debug("update() OK: {}", entity);
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            log.error("Error en update(): {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar entidad", e);
        }
    }

    @Override
    public void delete(T entity) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            // merge primero por si la entidad está detached
            T managed = session.merge(entity);
            session.remove(managed);
            tx.commit();
            log.debug("delete() OK: {}", entity);
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            log.error("Error en delete(): {}", e.getMessage(), e);
            throw new RuntimeException("Error al eliminar entidad", e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        try (Session session = openSession()) {
            T result = session.find(entityClass, id);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            log.error("Error en findById({}): {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al buscar entidad por ID", e);
        }
    }

    @Override
    public List<T> findAll() {
        try (Session session = openSession()) {
            String hql = "FROM " + entityClass.getSimpleName();
            return session.createQuery(hql, entityClass).list();
        } catch (Exception e) {
            log.error("Error en findAll() de {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Error al listar entidades", e);
        }
    }
}