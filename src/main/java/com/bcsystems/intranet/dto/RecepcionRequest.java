package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RecepcionRequest(
    @NotNull Integer idPedido,
    @NotNull List<RecepcionDetalleRequest> detalles
) {
    public record RecepcionDetalleRequest(
        @NotNull Integer idPedidoDetalle,
        @NotNull Integer cantidadRecibida,
        Double precioCompraUnitario,
        Double precioVentaSugerido,
        Double margenPorcentaje
    ) {}
}
