package com.splitup.service.dto;

import com.splitup.model.User;
import java.math.BigDecimal;

/**
 * Transferencia sugerida para saldar una deuda dentro del grupo.
 * {@code from} debe pagar {@code amount} a {@code to}.
 */
public record Settlement(User from, User to, BigDecimal amount) {}
