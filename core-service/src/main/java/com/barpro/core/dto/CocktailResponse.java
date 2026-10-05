package com.barpro.core.dto;

import com.barpro.core.entity.Cocktail;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record CocktailResponse(
        Long id,
        String name,
        String category,
        String ingredients,
        String imageUrl,
        boolean active,
        List<EventTypeRef> eventTypes,
        Instant createdAt
) {
    public record EventTypeRef(Long id, String code, String name) {
    }

    public static CocktailResponse from(Cocktail cocktail) {
        List<EventTypeRef> refs = cocktail.getEventTypes().stream()
                .sorted(Comparator.comparing(eventType -> eventType.getSortOrder()))
                .map(eventType -> new EventTypeRef(
                        eventType.getId(),
                        eventType.getCode(),
                        eventType.getName()))
                .toList();
        return new CocktailResponse(
                cocktail.getId(),
                cocktail.getName(),
                cocktail.getCategory().name(),
                cocktail.getIngredients(),
                cocktail.getImageUrl(),
                cocktail.isActive(),
                refs,
                cocktail.getCreatedAt());
    }
}
