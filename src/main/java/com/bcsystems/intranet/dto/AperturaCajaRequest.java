package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotNull;

public record AperturaCajaRequest(
        @NotNull Double saldoInicial
) {}
