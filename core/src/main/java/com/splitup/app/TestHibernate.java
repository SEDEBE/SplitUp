package com.splitup.app;

import com.splitup.model.User;
import com.splitup.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestHibernate {

    public static void main(String[] args) {
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User user = new User("Juan", "juan@splitup.dev");
            session.persist(user);

            tx.commit();
            System.out.println("Usuario guardado correctamente.");
        } catch (Exception e) {
            if (tx != null && tx.isActive())
                tx.rollback();
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
