package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotNull;

public record InventarioSucursalRequest(
    @NotNull Integer idSucursal,
    @NotNull Integer stock
) {}
