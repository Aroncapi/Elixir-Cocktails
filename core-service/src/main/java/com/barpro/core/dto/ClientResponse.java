package com.barpro.core.dto;

import com.barpro.core.entity.Client;

import java.math.BigDecimal;
import java.time.Instant;

public record ClientResponse(
        Long id,
        String name,
        String email,
        String whatsapp,
        String location,
        String tier,
        BigDecimal totalSpent,
        int reservations,
        String lastEvent,
        boolean active,
        Instant createdAt
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getWhatsapp(),
                client.getLocation(),
                client.getTier().name(),
                client.getTotalSpent(),
                client.getReservations(),
                client.getLastEvent(),
                client.isActive(),
                client.getCreatedAt());
    }
}
