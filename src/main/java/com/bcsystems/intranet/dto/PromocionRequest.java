package com.bcsystems.intranet.dto;

import com.bcsystems.intranet.domain.en.TipoPromocion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record PromocionRequest(
    @NotBlank String nombre,
    String descripcion,
    @NotNull TipoPromocion tipo,
    @NotNull Double descuentoPorcentaje,
    Double precioSugerido,
    Double precioFinal,
    Integer idProducto,
    @NotNull Boolean activo,
    @NotNull LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    List<PromocionDetalleRequest> detalles
) {}
