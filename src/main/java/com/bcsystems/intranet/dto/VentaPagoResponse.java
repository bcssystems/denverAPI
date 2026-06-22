package com.bcsystems.intranet.dto;

public record VentaPagoResponse(
        Integer idVentaPago,
        Integer idTipoPago,
        String tipoPagoNombre,
        Double monto,
        String referencia
) {}
