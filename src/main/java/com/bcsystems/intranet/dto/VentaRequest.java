package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VentaRequest(
        @NotNull Integer idCaja,
        Integer idCliente,
        @NotNull String tipoVenta,
        @NotNull Integer precioSeleccionado,
        @NotNull Double subtotal,
        @NotNull Double descuento,
        @NotNull Double total,
        @NotEmpty List<VentaDetalleRequest> detalles
) {}
