package com.barpro.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SettingsUpdateRequest(
        @NotBlank(message = "El WhatsApp es obligatorio")
        @Pattern(regexp = "\\+?\\d{8,15}", message = "WhatsApp invalido: usa solo digitos con prefijo de pais, ej. +51937336603")
        String whatsappOwner,
        @Email(message = "Correo de contacto invalido")
        @Size(max = 150)
        String contactEmail,
        @Size(max = 80)
        String businessName
) {
}
