package com.bcsystems.intranet.dto;

import com.bcsystems.intranet.domain.en.EstadoCredito;
import java.time.LocalDateTime;

public record CreditoResponse(
        Integer idCredito,
        Integer idVenta,
        Integer folioVenta,
        Integer idCliente,
        String clienteNombre,
        Double montoOriginal,
        Double saldoPendiente,
        Integer plazoMeses,
        Double porcentajeInteres,
        LocalDateTime fechaVencimiento,
        EstadoCredito estado,
        LocalDateTime fechaCreacion
) {}
