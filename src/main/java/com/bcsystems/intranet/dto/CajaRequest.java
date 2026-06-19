package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CajaRequest(
        @NotBlank String nombre,
        @NotNull Integer idSucursal
) {}
