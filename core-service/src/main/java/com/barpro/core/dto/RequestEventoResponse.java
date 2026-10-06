package com.barpro.core.dto;

import com.barpro.core.entity.Request;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public record RequestEventoResponse(
        Long id,
        String folio,
        String eventDate,
        String eventTime,
        String status,
        String eventTypeName,
        String clientName,
        String location,
        BigDecimal totalAmount) {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static RequestEventoResponse from(Request request) {
        return new RequestEventoResponse(
                request.getId(),
                request.getFolio(),
                request.getEventDate() == null ? null : request.getEventDate().format(DIA),
                request.getEventTime() == null ? null : request.getEventTime().format(HORA),
                request.getStatus().name(),
                request.getEventType().getName(),
                request.getClientName(),
                request.getLocation(),
                request.getTotalAmount());
    }
}
