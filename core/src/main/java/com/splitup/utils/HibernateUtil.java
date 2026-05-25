package com.splitup.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.InputStream;
import java.util.Properties;

public class HibernateUtil {

    private static SessionFactory sessionFactory;
    private static Throwable initError;

    static {
        try {
            Properties props = new Properties();
            try (InputStream in = HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate.properties")) {
                if (in == null) {
                    throw new IllegalStateException("No se encontró hibernate.properties en src/main/resources");
                }
                props.load(in);
            }

            sessionFactory = new Configuration()
                    .addProperties(props)
                    .addAnnotatedClass(com.splitup.model.User.class)
                    .addAnnotatedClass(com.splitup.model.ExpenseGroup.class)
                    .addAnnotatedClass(com.splitup.model.GroupMember.class)
                    .addAnnotatedClass(com.splitup.model.Category.class)
                    .addAnnotatedClass(com.splitup.model.AuthIdentity.class)
                    .addAnnotatedClass(com.splitup.model.Expense.class)
                    .addAnnotatedClass(com.splitup.model.ExpenseShare.class)
                    .addAnnotatedClass(com.splitup.model.Attachment.class)
                    .addAnnotatedClass(com.splitup.model.SettlementRecord.class)
                    .buildSessionFactory();

        } catch (Throwable ex) {
            System.err.println("Base de datos no disponible: " + ex.getMessage());
            initError = ex;
            sessionFactory = null;
        }
    }

    public static boolean isAvailable() {
        return sessionFactory != null;
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new RuntimeException(
                "Base de datos no disponible. Comprueba que MySQL está activo en localhost:3306/splitup.",
                initError);
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
