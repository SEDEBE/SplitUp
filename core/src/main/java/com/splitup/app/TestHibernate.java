package com.splitup.app;

import com.splitup.model.User;
import com.splitup.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestHibernate {

    private static final String TEST_EMAIL = "test@splitup.dev";

    public static void main(String[] args) {

        Long userId = createOrUpdateTestUser("Alex");

        readUserById(userId);

        HibernateUtil.shutdown();
    }

    /**
     * - Si el usuario existe -> update
     * - Si no existe -> insert
     */
    private static Long createOrUpdateTestUser(String name) {
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User user = session
                    .createQuery(
                            "from User u where u.email = :email",
                            User.class)
                    .setParameter("email", TEST_EMAIL)
                    .uniqueResult();

            if (user == null) {
                user = new User(name, TEST_EMAIL);
                session.persist(user);
                System.out.println("Usuario de test creado");
            } else {
                user.setName(name + " (updated)");
                System.out.println("Usuario de test actualizado");
            }

            tx.commit();
            return user.getId();

        } catch (Exception e) {
            safeRollback(tx);
            throw e;
        }
    }

    private static void readUserById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if (user != null) {
                System.out.println(
                        "Lectura OK: id=" + user.getId()
                                + ", name=" + user.getName()
                                + ", email=" + user.getEmail());
            }
        }
    }

    private static void safeRollback(Transaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }
}
