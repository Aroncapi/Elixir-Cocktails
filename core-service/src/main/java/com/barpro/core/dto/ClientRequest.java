package com.barpro.core.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ClientRequest(
        @NotBlank @Size(max = 120) String name,
        @Email @Size(max = 150) String email,
        @Size(max = 30) String whatsapp,
        @Size(max = 200) String location,
        @NotBlank @Size(max = 20) String tier,
        @DecimalMin(value = "0") BigDecimal totalSpent,
        Boolean active
) {
}
