package com.barpro.core.dto;

import com.barpro.core.entity.Client;
import com.barpro.core.entity.Request;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public record SolicitudResponse(
        Long id,
        String folio,
        String publicToken,
        String status,
        Long eventTypeId,
        String eventTypeName,
        String eventDate,
        String eventTime,
        int guests,
        int durationHours,
        String location,
        String notes,
        String level,
        int extraBartenders,
        Breakdown breakdown,
        List<CocktailRef> cocktails,
        ClientRef client,
        Instant createdAt
) {
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public record Breakdown(BigDecimal base, BigDecimal premium, BigDecimal personal, BigDecimal total) {
    }

    public record CocktailRef(Long id, String name, String ingredients, String imageUrl, String category) {
    }

    public record ClientRef(Long id, String name, String email, String whatsapp) {
    }

    public static SolicitudResponse from(Request request) {
        List<CocktailRef> refs = request.getCocktails().stream()
                .sorted(Comparator.comparing(cocktail -> cocktail.getName().toLowerCase()))
                .map(cocktail -> new CocktailRef(
                        cocktail.getId(),
                        cocktail.getName(),
                        cocktail.getIngredients(),
                        cocktail.getImageUrl(),
                        cocktail.getCategory().name()))
                .toList();
        Client client = request.getClient();
        return new SolicitudResponse(
                request.getId(),
                request.getFolio(),
                request.getPublicToken(),
                request.getStatus().name(),
                request.getEventType().getId(),
                request.getEventType().getName(),
                request.getEventDate().format(DIA),
                request.getEventTime() == null ? null : request.getEventTime().format(HORA),
                request.getGuests(),
                request.getDurationHours(),
                request.getLocation(),
                request.getNotes(),
                request.getLevel().name(),
                request.getExtraBartenders(),
                new Breakdown(
                        request.getBaseAmount(),
                        request.getPremiumAmount(),
                        request.getPersonalAmount(),
                        request.getTotalAmount()),
                refs,
                client == null
                        ? new ClientRef(null, request.getClientName(), request.getClientEmail(), request.getClientWhatsapp())
                        : new ClientRef(client.getId(), request.getClientName(), request.getClientEmail(), request.getClientWhatsapp()),
                request.getCreatedAt());
    }
}
