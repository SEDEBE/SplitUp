package com.splitup.server.dto;

import com.splitup.model.User;

public record UserDto(Long id, String name, String email, String avatarUrl) {
    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getAvatarUrl());
    }
}
