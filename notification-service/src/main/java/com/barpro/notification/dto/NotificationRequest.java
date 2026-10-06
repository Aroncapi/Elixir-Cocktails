package com.barpro.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
        @NotBlank(message = "el tipo es obligatorio") String type,
        @NotBlank(message = "el canal es obligatorio") String channel,
        @NotBlank(message = "el destinatario es obligatorio") @Size(max = 150) String recipient,
        @NotBlank(message = "el asunto es obligatorio") @Size(max = 200) String subject,
        @NotBlank(message = "el cuerpo es obligatorio") @Size(max = 5000) String body,
        Long referenceId) {
}
