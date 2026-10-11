package com.barpro.core.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SettingsUpdateRequest(
        @NotBlank(message = "El WhatsApp es obligatorio")
        @Pattern(regexp = "\\+?\\d{8,15}", message = "WhatsApp invalido: usa solo digitos con prefijo de pais, ej. +51937336603")
        String whatsappOwner,
        @Email(message = "Correo de contacto invalido")
        @Size(max = 150)
        String contactEmail,
        @Size(max = 80)
        String businessName,
        @DecimalMin(value = "0.1", message = "El tipo de cambio debe estar entre 0.1 y 100")
        @DecimalMax(value = "100", message = "El tipo de cambio debe estar entre 0.1 y 100")
        BigDecimal exchangeRate
) {
}
