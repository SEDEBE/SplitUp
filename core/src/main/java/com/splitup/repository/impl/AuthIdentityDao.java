package com.splitup.repository.impl;

import com.splitup.model.AuthIdentity;
import com.splitup.model.enums.AuthProvider;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AuthIdentityDao extends AbstractDao<AuthIdentity, Long> {

    private static final Logger log = LoggerFactory.getLogger(AuthIdentityDao.class);

    public AuthIdentityDao() {
        super(AuthIdentity.class);
    }

    /**
     * Busca una identidad de autenticación por email y proveedor.
     * Hace JOIN FETCH del usuario para evitar LazyInitializationException.
     */
    public Optional<AuthIdentity> findByEmailAndProvider(String email, AuthProvider provider) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM AuthIdentity ai JOIN FETCH ai.user WHERE ai.emailAtProvider = :email AND ai.provider = :provider",
                    AuthIdentity.class)
                    .setParameter("email", email.trim().toLowerCase())
                    .setParameter("provider", provider)
                    .uniqueResultOptional();
        } catch (Exception e) {
            log.error("Error en findByEmailAndProvider({}, {}): {}", email, provider, e.getMessage(), e);
            throw new RuntimeException("Error al buscar identidad de autenticación", e);
        }
    }
}
