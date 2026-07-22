package com.bcsystems.intranet.dto;

public record CorteDetallePagoDto(
        Integer idTipoPago,
        String tipoPagoNombre,
        Double monto,
        Double montoReal
) {}
