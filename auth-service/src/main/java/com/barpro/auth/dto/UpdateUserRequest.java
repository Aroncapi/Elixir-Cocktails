package com.barpro.auth.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 100) String fullName,
        String role,
        Boolean active,
        @Size(min = 8, max = 72) String password
) {
}
