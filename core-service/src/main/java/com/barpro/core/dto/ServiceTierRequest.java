package com.barpro.core.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServiceTierRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 80) String name,
        @Size(max = 500) String description,
        @NotBlank String tierType,
        @DecimalMin(value = "0") BigDecimal price,
        Boolean active
) {
}
