package com.bcsystems.intranet.dto;

public record CarritoItemRapidoRequest(
        Integer idCaja,
        String descripcion,
        Double precioVenta,
        Double precioCompra,
        Integer cantidad
) {}
