package com.barpro.core.dto;

import com.barpro.core.entity.ServiceTier;

import java.math.BigDecimal;

public record ServiceTierResponse(
        Long id,
        String code,
        String name,
        String description,
        String tierType,
        BigDecimal price,
        boolean active
) {
    public static ServiceTierResponse from(ServiceTier tier) {
        return new ServiceTierResponse(
                tier.getId(),
                tier.getCode(),
                tier.getName(),
                tier.getDescription(),
                tier.getTierType().name(),
                tier.getPrice(),
                tier.isActive());
    }
}
