package com.bcsystems.intranet.dto;

import com.bcsystems.intranet.domain.en.TipoMovimiento;
import jakarta.validation.constraints.NotNull;

public record MovimientoStockRequest(
    @NotNull TipoMovimiento tipoMovimiento,
    @NotNull Integer cantidad,
    Integer idSucursal,
    String referencia,
    String observacion
) {}
