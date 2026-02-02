package com.splitup.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.InputStream;
import java.util.Properties;

public class HibernateUtil {

    private static final SessionFactory sessionFactory;

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
                    .buildSessionFactory();

        } catch (Throwable ex) {
            System.err.println("Error al crear SessionFactory");
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
