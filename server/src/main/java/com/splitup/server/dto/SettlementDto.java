package com.splitup.server.dto;

import com.splitup.service.dto.Settlement;

import java.math.BigDecimal;

public record SettlementDto(Long fromId, String fromName, Long toId, String toName, BigDecimal amount) {
    public static SettlementDto from(Settlement s) {
        return new SettlementDto(
                s.from().getId(), s.from().getName(),
                s.to().getId(),   s.to().getName(),
                s.amount()
        );
    }
}
