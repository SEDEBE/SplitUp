package com.splitup.repository.impl;

import com.splitup.model.ExpenseGroup;
import com.splitup.model.SettlementRecord;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * DAO para la entidad {@link SettlementRecord}.
 * Extiende {@link AbstractDao} para operaciones CRUD genéricas y añade
 * una consulta con JOIN FETCH para evitar LazyInitializationException.
 */
public class SettlementRecordDao extends AbstractDao<SettlementRecord, Long> {

    private static final Logger log = LoggerFactory.getLogger(SettlementRecordDao.class);

    public SettlementRecordDao() {
        super(SettlementRecord.class);
    }

    /**
     * Devuelve los pagos confirmados del grupo con {@code fromUser} y
     * {@code toUser} ya inicializados mediante JOIN FETCH, de modo que
     * los servicios pueden acceder a sus nombres sin sesión abierta.
     *
     * @param group Grupo
     * @return Lista ordenada de más reciente (settledAt DESC) a más antiguo
     */
    public List<SettlementRecord> findByGroupFetchingUsers(ExpenseGroup group) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM SettlementRecord sr " +
                    "JOIN FETCH sr.fromUser " +
                    "JOIN FETCH sr.toUser " +
                    "WHERE sr.group = :group " +
                    "ORDER BY sr.settledAt DESC",
                    SettlementRecord.class)
                    .setParameter("group", group)
                    .list();
        } catch (Exception e) {
            log.error("Error en findByGroupFetchingUsers(groupId={}): {}", group.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al obtener liquidaciones del grupo", e);
        }
    }

    /**
     * Busca un registro por id con fromUser, toUser y group cargados.
     * Se usa tras un save() para devolver el record completo con settledAt
     * generado por la BD.
     *
     * @param id PK del registro
     * @return Optional con el SettlementRecord completo, o vacío si no existe
     */
    public Optional<SettlementRecord> findByIdFetchingAll(Long id) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM SettlementRecord sr " +
                    "JOIN FETCH sr.fromUser " +
                    "JOIN FETCH sr.toUser " +
                    "JOIN FETCH sr.group " +
                    "WHERE sr.id = :id",
                    SettlementRecord.class)
                    .setParameter("id", id)
                    .uniqueResultOptional();
        } catch (Exception e) {
            log.error("Error en findByIdFetchingAll(id={}): {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al buscar liquidación", e);
        }
    }
}
