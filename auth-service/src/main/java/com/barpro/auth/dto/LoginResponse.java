package com.barpro.auth.dto;

public record LoginResponse(String token, String type, UserResponse user) {
}
