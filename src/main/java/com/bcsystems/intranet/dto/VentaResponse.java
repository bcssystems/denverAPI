package com.bcsystems.intranet.dto;

import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
        Integer idVenta,
        Integer idCaja,
        String cajaNombre,
        Integer idCliente,
        String clienteNombre,
        String usuario,
        String tipoVenta,
        Integer precioSeleccionado,
        Double subtotal,
        Double descuento,
        Double total,
        String estado,
        LocalDateTime fecha,
        List<VentaDetalleResponse> detalles
) {}
