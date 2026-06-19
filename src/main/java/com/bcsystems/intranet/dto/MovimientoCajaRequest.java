package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MovimientoCajaRequest(
        @NotNull Double monto,
        @NotBlank String motivo
) {}
