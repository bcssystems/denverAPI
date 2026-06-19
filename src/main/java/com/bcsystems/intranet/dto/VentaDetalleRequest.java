package com.bcsystems.intranet.dto;

public record VentaDetalleRequest(
        Integer idProducto,
        String descripcion,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal
) {}
