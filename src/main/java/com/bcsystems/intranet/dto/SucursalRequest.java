package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotBlank;

public record SucursalRequest(
    @NotBlank String nombre,
    String direccion,
    String telefono,
    Boolean activa
) {}
