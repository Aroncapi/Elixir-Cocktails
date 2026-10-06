package com.barpro.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StatusUpdateRequest(
        @NotBlank @Size(max = 20) String status
) {
}
