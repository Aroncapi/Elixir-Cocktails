package com.barpro.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SolicitudRequest(
        @NotNull Long eventTypeId,
        @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Use el formato yyyy-MM-dd")
        String eventDate,
        @Pattern(regexp = "\\d{2}:\\d{2}", message = "Use el formato HH:mm")
        String eventTime,
        @NotNull @Min(1) @Max(100000) Integer guests,
        @NotNull @Min(1) @Max(24) Integer durationHours,
        @Size(max = 200) String location,
        @Size(max = 1000) String notes,
        @NotBlank String level,
        @Min(0) @Max(20) Integer extraBartenders,
        @NotNull List<Long> cocktailIds,
        @NotBlank @Size(max = 120) String clientName,
        @NotBlank @Size(max = 30) String clientWhatsapp,
        @NotBlank @Email @Size(max = 150) String clientEmail
) {
}
