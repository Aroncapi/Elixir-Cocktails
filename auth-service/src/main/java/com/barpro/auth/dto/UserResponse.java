package com.barpro.auth.dto;

import com.barpro.auth.entity.User;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String role,
        boolean active,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt());
    }
}
