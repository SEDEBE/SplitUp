package com.splitup.repository;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica DAO.
 *
 * @param <T>  Tipo de la entidad
 * @param <ID> Tipo de la clave primaria
 */
public interface GenericDao<T, ID> {

    /** Persiste una nueva entidad en la BD. */
    void save(T entity);

    /** Actualiza una entidad ya existente. */
    void update(T entity);

    /** Elimina una entidad. */
    void delete(T entity);

    /** Busca por clave primaria. Devuelve Optional vacío si no existe. */
    Optional<T> findById(ID id);

    /** Devuelve todas las filas de la tabla. */
    List<T> findAll();
}