package com.splitup.server.dto;

import com.splitup.model.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseDto(
        Long id,
        Long groupId,
        Long payerId,
        String payerName,
        String title,
        BigDecimal totalAmount,
        String currency,
        LocalDate expenseDate,
        String splitMode,
        String note
) {
    public static ExpenseDto from(Expense e) {
        return new ExpenseDto(
                e.getId(),
                e.getGroup().getId(),
                e.getPayer().getId(),
                e.getPayer().getName(),
                e.getTitle(),
                e.getTotalAmount(),
                e.getCurrency(),
                e.getExpenseDate(),
                e.getSplitMode().name(),
                e.getNote()
        );
    }
}
