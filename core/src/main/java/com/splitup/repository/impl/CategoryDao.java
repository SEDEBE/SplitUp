package com.splitup.repository.impl;

import com.splitup.model.Category;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * DAO para la entidad Category.
 */
public class CategoryDao extends AbstractDao<Category, Long> {

    private static final Logger log = LoggerFactory.getLogger(CategoryDao.class);

    public CategoryDao() {
        super(Category.class);
    }

    /**
     * Busca una categoría por su nombre exacto (case-insensitive).
     *
     * @param name Nombre de la categoría
     * @return Optional con la categoría
     */
    public Optional<Category> findByName(String name) {
        if (name == null || name.isBlank())
            return Optional.empty();
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Category c WHERE LOWER(c.name) = :name", Category.class)
                    .setParameter("name", name.trim().toLowerCase())
                    .uniqueResultOptional();
        } catch (Exception e) {
            log.error("Error en findByName({}): {}", name, e.getMessage(), e);
            throw new RuntimeException("Error al buscar categoría por nombre", e);
        }
    }

    /**
     * Devuelve todas las categorías ordenadas alfabéticamente.
     *
     * @return Lista de categorías
     */
    public List<Category> findAllOrdered() {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Category c ORDER BY c.name", Category.class)
                    .list();
        } catch (Exception e) {
            log.error("Error en findAllOrdered(): {}", e.getMessage(), e);
            throw new RuntimeException("Error al listar categorías", e);
        }
    }
}