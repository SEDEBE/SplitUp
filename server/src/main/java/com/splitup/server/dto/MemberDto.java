package com.splitup.server.dto;

import com.splitup.model.GroupMember;

public record MemberDto(Long userId, String name, String email, String role) {
    public static MemberDto from(GroupMember m) {
        return new MemberDto(
                m.getUser().getId(),
                m.getUser().getName(),
                m.getUser().getEmail(),
                m.getRole().name()
        );
    }
}
