package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductoRequest(
    @NotBlank String nombre,
    String descripcion,
    Double precio1,
    Double precio2,
    Double precio3,
    Double precio4,
    @NotNull Integer stockActual,
    Integer stockMinimo,
    Integer stockMaximo,
    Boolean activo,
    Integer idSucursal
) {}
