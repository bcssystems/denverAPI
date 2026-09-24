package com.bcsystems.intranet.dto;

import java.util.List;

public record EstadoCuentaResponse(
        CreditoResponse credito,
        ClienteResponse cliente,
        List<AbonoResponse> abonos,
        List<MovimientoCreditoResponse> movimientos
) {}