package com.barpro.core.dto;

import java.math.BigDecimal;

public record RequestResumenResponse(
        long solicitudes,
        BigDecimal ingresosEstimados,
        long proximosEventos,
        long pendientes,
        long confirmadas,
        long canceladas) {
}
