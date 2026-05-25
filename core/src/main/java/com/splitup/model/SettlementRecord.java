package com.splitup.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pago CONFIRMADO de una deuda entre dos miembros de un grupo.
 *
 * <p><strong>Distinción clave:</strong> no confundir con
 * {@link com.splitup.service.dto.Settlement}, que es una transferencia
 * <em>sugerida</em> por el algoritmo greedy y que nunca se persiste.
 * Esta entidad representa el acto real de que {@code fromUser} ha pagado
 * {@code amount} euros a {@code toUser}, y queda almacenada en la tabla
 * {@code settlements} para que los cálculos de balance futuros la tengan
 * en cuenta.
 */
@Entity
@Table(name = "settlements")
public class SettlementRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ExpenseGroup group;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Generado por la BD (DEFAULT CURRENT_TIMESTAMP); no se inserta desde Java.
    @Column(name = "settled_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime settledAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public SettlementRecord() {}

    public SettlementRecord(ExpenseGroup group, User fromUser, User toUser,
                            BigDecimal amount, User createdBy) {
        this.group     = group;
        this.fromUser  = fromUser;
        this.toUser    = toUser;
        this.amount    = amount;
        this.createdBy = createdBy;
    }

    public Long          getId()        { return id; }
    public ExpenseGroup  getGroup()     { return group; }
    public User          getFromUser()  { return fromUser; }
    public User          getToUser()    { return toUser; }
    public BigDecimal    getAmount()    { return amount; }
    public LocalDateTime getSettledAt() { return settledAt; }
    public User          getCreatedBy() { return createdBy; }
}
