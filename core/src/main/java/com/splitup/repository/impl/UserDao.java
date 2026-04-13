package com.splitup.repository.impl;

import com.splitup.model.User;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * DAO para la entidad User.
 * Extiende AbstractDao con consultas específicas de usuario.
 */
public class UserDao extends AbstractDao<User, Long> {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    public UserDao() {
        super(User.class);
    }

    /**
     * Busca un usuario por su email (único en BD).
     *
     * @param email Email a buscar
     * @return Optional con el usuario, o vacío si no existe
     */
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank())
            return Optional.empty();
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email.trim().toLowerCase())
                    .uniqueResultOptional();
        } catch (Exception e) {
            log.error("Error en findByEmail({}): {}", email, e.getMessage(), e);
            throw new RuntimeException("Error al buscar usuario por email", e);
        }
    }

    /**
     * Busca usuarios cuyo nombre de pantalla contenga el texto dado.
     *
     * @param namePart Texto parcial del nombre (case-insensitive)
     * @return Lista de usuarios que coinciden
     */
    public List<User> findByNameContaining(String namePart) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM User u WHERE LOWER(u.name) LIKE :pattern", User.class)
                    .setParameter("pattern", "%" + namePart.trim().toLowerCase() + "%")
                    .list();
        } catch (Exception e) {
            log.error("Error en findByNameContaining({}): {}", namePart, e.getMessage(), e);
            throw new RuntimeException("Error al buscar usuarios por nombre", e);
        }
    }

    /**
     * Comprueba si ya existe un usuario con ese email.
     *
     * @param email Email a comprobar
     * @return true si ya está registrado
     */
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}