package com.barpro.core.dto;

import com.barpro.core.entity.EventType;

public record EventTypeResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        int sortOrder
) {
    public static EventTypeResponse from(EventType eventType) {
        return new EventTypeResponse(
                eventType.getId(),
                eventType.getCode(),
                eventType.getName(),
                eventType.getDescription(),
                eventType.isActive(),
                eventType.getSortOrder());
    }
}
