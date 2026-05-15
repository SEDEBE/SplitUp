package com.splitup.service;

import com.splitup.model.AuthIdentity;
import com.splitup.model.User;
import com.splitup.model.enums.AuthProvider;
import com.splitup.repository.impl.AuthIdentityDao;
import com.splitup.repository.impl.UserDao;
import com.splitup.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final AuthIdentityDao authIdentityDao;

    public UserService() {
        this.userDao = new UserDao();
        this.authIdentityDao = new AuthIdentityDao();
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
        this.authIdentityDao = new AuthIdentityDao();
    }

    // -----------------------------------------------------------------------
    // Autenticación con contraseña
    // -----------------------------------------------------------------------

    /**
     * Registra un nuevo usuario con contraseña local (BCrypt).
     * Crea el User y su AuthIdentity(LOCAL) en una sola transacción.
     */
    public User registerWithPassword(String name, String email, String password) throws BusinessException {
        if (name == null || name.isBlank())
            throw new BusinessException("El nombre no puede estar vacío");
        if (email == null || email.isBlank())
            throw new BusinessException("El email no puede estar vacío");
        if (password == null || password.length() < 6)
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres");

        String normalizedEmail = email.trim().toLowerCase();
        if (userDao.existsByEmail(normalizedEmail))
            throw new BusinessException("Ya existe un usuario con el email: " + normalizedEmail);

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            User user = new User(name.trim(), normalizedEmail);
            session.persist(user);

            // Guardamos el hash en provider_user_id (campo reutilizado para auth local)
            AuthIdentity identity = new AuthIdentity(user, AuthProvider.LOCAL, hash);
            identity.setEmailAtProvider(normalizedEmail);
            session.persist(identity);

            tx.commit();
            log.info("Usuario registrado con contraseña: id={}, email={}", user.getId(), normalizedEmail);
            return user;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("Error al registrar usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error al registrar usuario", e);
        } finally {
            session.close();
        }
    }

    /**
     * Autentica con email y contraseña.
     * Devuelve el User si las credenciales son correctas, vacío si no.
     */
    public Optional<User> loginWithPassword(String email, String password) {
        if (email == null || password == null) return Optional.empty();
        Optional<AuthIdentity> identity = authIdentityDao.findByEmailAndProvider(
                email.trim().toLowerCase(), AuthProvider.LOCAL);
        if (identity.isEmpty()) return Optional.empty();

        AuthIdentity ai = identity.get();
        if (!BCrypt.checkpw(password, ai.getProviderUserId())) return Optional.empty();

        return Optional.of(ai.getUser());
    }

    // -----------------------------------------------------------------------
    // Operaciones
    // -----------------------------------------------------------------------

    public User register(String name, String email) throws BusinessException {
        if (name == null || name.isBlank())
            throw new BusinessException("El nombre no puede estar vacío");
        if (email == null || email.isBlank())
            throw new BusinessException("El email no puede estar vacío");

        String normalizedEmail = email.trim().toLowerCase();
        if (userDao.existsByEmail(normalizedEmail))
            throw new BusinessException("Ya existe un usuario con el email: " + normalizedEmail);

        User user = new User(name.trim(), normalizedEmail);
        userDao.save(user);
        log.info("Usuario registrado: id={}, email={}", user.getId(), normalizedEmail);
        return user;
    }

    public void updateProfile(User user, String newName) throws BusinessException {
        if (newName == null || newName.isBlank())
            throw new BusinessException("El nombre no puede estar vacío");
        user.setName(newName.trim());
        userDao.update(user);
        log.info("Perfil actualizado: userId={}", user.getId());
    }

    public void updateAvatar(User user, String avatarUrl) {
        user.setAvatarUrl(avatarUrl);
        userDao.update(user);
        log.info("Avatar actualizado: userId={}", user.getId());
    }

    // -----------------------------------------------------------------------
    // Consultas
    // -----------------------------------------------------------------------

    public Optional<User> findById(Long id) {
        return userDao.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public List<User> searchByName(String namePart) {
        return userDao.findByNameContaining(namePart);
    }
}
