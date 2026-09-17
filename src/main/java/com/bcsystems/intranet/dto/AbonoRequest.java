package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotNull;

public record AbonoRequest(
        @NotNull Integer idCredito,
        @NotNull Double monto,
        String tipo,
        Integer idTipoPago,
        Integer idCaja
) {}
