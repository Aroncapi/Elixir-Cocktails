package com.barpro.core.dto;

public record SettingsResponse(
        String whatsappOwner,
        String contactEmail,
        String businessName
) {
}
