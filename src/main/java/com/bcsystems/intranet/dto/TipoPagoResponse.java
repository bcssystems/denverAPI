package com.bcsystems.intranet.dto;

public record TipoPagoResponse(
        Integer idTipoPago,
        String nombre,
        Boolean activo
) {}
