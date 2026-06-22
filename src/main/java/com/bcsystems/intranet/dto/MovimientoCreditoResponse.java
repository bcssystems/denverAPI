package com.bcsystems.intranet.dto;

import com.bcsystems.intranet.domain.en.TipoMovimientoCredito;
import java.time.LocalDateTime;

public record MovimientoCreditoResponse(
        Integer idMovimiento,
        Integer idCredito,
        TipoMovimientoCredito tipo,
        Double monto,
        Double saldoAnterior,
        Double saldoNuevo,
        String descripcion,
        LocalDateTime fecha,
        String usuario
) {}
