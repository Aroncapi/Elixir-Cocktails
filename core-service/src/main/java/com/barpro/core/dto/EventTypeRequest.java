package com.barpro.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EventTypeRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 80) String name,
        @Size(max = 255) String description,
        Integer sortOrder,
        Boolean active
) {
}
