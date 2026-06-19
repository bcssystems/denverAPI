package com.bcsystems.intranet.dto;

import java.time.LocalDateTime;

public record GastoResponse(
        Integer idGasto,
        Integer idCaja,
        String cajaNombre,
        String descripcion,
        Double monto,
        String usuario,
        String autorizador,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaAutorizacion
) {}
