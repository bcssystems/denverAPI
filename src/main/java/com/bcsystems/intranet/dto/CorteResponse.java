package com.bcsystems.intranet.dto;

import java.time.LocalDateTime;

public record CorteResponse(
        Integer idCorte,
        Integer idCaja,
        String cajaNombre,
        Double saldoInicial,
        Double totalVentas,
        Double totalVentasContado,
        Double totalVentasCredito,
        Double totalIngresos,
        Double totalEgresos,
        Double saldoFinalContado,
        Double saldoEsperado,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        String usuario
) {}
