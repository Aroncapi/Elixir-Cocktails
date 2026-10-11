package com.barpro.core.dto;

import java.math.BigDecimal;

public record SettingsResponse(
        String whatsappOwner,
        String contactEmail,
        String businessName,
        BigDecimal exchangeRate,
        int baseHours,
        BigDecimal extraHourPen
) {
}
