package com.barpro.core.dto;

import java.util.List;

public record SolicitudPageResponse(
        List<SolicitudResponse> items,
        long total,
        int page,
        int size,
        int totalPages) {
}
