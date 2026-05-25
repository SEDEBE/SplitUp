package com.splitup.server.dto;

import com.splitup.model.ExpenseGroup;

public record GroupDto(Long id, String name, String description, Long createdById, String inviteCode) {
    public static GroupDto from(ExpenseGroup g) {
        return new GroupDto(g.getId(), g.getName(), g.getDescription(),
                g.getCreatedBy() != null ? g.getCreatedBy().getId() : null,
                g.getInviteCode());
    }
}
