package com.bcsystems.intranet.dto;

import java.time.LocalDateTime;

public record CodigoAutorizacionResponse(
        Integer idSolicitud,
        Integer idVenta,
        String codigo,
        LocalDateTime generadoEn,
        LocalDateTime expiraEn
) {}