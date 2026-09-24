package com.bcsystems.intranet.dto;

public record CancelarVentaRequest(
        String codigo,
        String motivo
) {}