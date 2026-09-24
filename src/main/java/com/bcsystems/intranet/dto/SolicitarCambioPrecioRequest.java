package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitarCambioPrecioRequest(
        @NotNull Integer idProducto,
        @NotNull Integer idSucursal,
        @NotNull Double precioActual,
        @NotNull Double nuevoPrecio,
        @NotBlank String motivo
) {}