package com.splitup.server.dto;

import com.splitup.service.dto.UserBalance;

import java.math.BigDecimal;

public record BalanceDto(Long userId, String userName, BigDecimal balance, String status) {
    public static BalanceDto from(UserBalance ub) {
        int sign = ub.balance().compareTo(BigDecimal.ZERO);
        String status = sign > 0 ? "ACREEDOR" : sign < 0 ? "DEUDOR" : "SALDADO";
        return new BalanceDto(ub.user().getId(), ub.user().getName(), ub.balance(), status);
    }
}
