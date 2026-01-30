package com.splitup.app;

import com.splitup.model.User;
import com.splitup.utils.JpaUtil;
import jakarta.persistence.EntityManager;

public class TestHibernate {

    public static void main(String[] args) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();

        em.getTransaction().begin();

        User user = new User("Alex", "alex@splitup.dev");
        em.persist(user);

        em.getTransaction().commit();
        em.close();
        JpaUtil.shutdown();

        System.out.println("Usuario guardado con ID: " + user.getId());
    }

}
