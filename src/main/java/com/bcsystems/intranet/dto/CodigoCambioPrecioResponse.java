package com.bcsystems.intranet.dto;

import java.time.LocalDateTime;

public record CodigoCambioPrecioResponse(
        Integer idSolicitud,
        Integer idProducto,
        String codigo,
        LocalDateTime generadoEn,
        LocalDateTime expiraEn
) {}