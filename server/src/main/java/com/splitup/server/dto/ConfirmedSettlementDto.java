package com.splitup.server.dto;

import com.splitup.model.SettlementRecord;

import java.time.format.DateTimeFormatter;

public record ConfirmedSettlementDto(
        Long   id,
        Long   fromUserId,
        String fromName,
        Long   toUserId,
        String toName,
        double amount,
        String settledAt
) {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static ConfirmedSettlementDto from(SettlementRecord r) {
        return new ConfirmedSettlementDto(
                r.getId(),
                r.getFromUser().getId(), r.getFromUser().getName(),
                r.getToUser().getId(),   r.getToUser().getName(),
                r.getAmount().doubleValue(),
                r.getSettledAt() != null ? r.getSettledAt().format(FMT) : ""
        );
    }
}
