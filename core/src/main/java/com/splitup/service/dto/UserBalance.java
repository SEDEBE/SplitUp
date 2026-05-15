package com.splitup.service.dto;

import com.splitup.model.User;
import java.math.BigDecimal;

/**
 * Balance neto de un usuario dentro de un grupo.
 *
 * balance > 0  → el grupo le debe dinero (es acreedor).
 * balance < 0  → él debe dinero al grupo (es deudor).
 * balance = 0  → está saldado.
 */
public record UserBalance(User user, BigDecimal balance) {}
