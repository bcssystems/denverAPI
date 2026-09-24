package com.bcsystems.intranet.dto;

import java.time.LocalDateTime;

public record SolicitudCancelacionResponse(
        Integer idSolicitud,
        Integer idVenta,
        Integer idSucursal,
        String sucursalNombre,
        String clienteNombre,
        String cajero,
        Double total,
        String motivo,
        String estado,
        boolean codigoGenerado,
        LocalDateTime fechaSolicitud,
        String generadoPor,
        String autorizador,
        LocalDateTime fechaAutorizacion
) {}