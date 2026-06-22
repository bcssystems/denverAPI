package com.bcsystems.intranet.dto;

public record ReservaProductoResponse(
        Integer idReserva,
        Integer idCaja,
        Integer idProducto,
        Integer cantidad
) {}
