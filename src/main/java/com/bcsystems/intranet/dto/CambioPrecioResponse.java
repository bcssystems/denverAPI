package com.bcsystems.intranet.dto;

import java.time.LocalDateTime;

public record CambioPrecioResponse(
        Integer idSolicitud,
        Integer idProducto,
        String productoSku,
        String productoNombre,
        Integer idSucursal,
        String sucursalNombre,
        Double precioActual,
        Double nuevoPrecio,
        String motivo,
        String estado,
        boolean codigoGenerado,
        LocalDateTime fechaSolicitud,
        String solicitante,
        String generadoPor,
        String autorizador,
        LocalDateTime fechaAutorizacion
) {}