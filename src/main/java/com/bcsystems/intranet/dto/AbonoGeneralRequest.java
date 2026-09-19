package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotNull;

public record AbonoGeneralRequest(
        @NotNull Integer idCliente,
        @NotNull Double monto,
        Integer idTipoPago,
        Integer idCaja
) {}
