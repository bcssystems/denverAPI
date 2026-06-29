package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotBlank;

public record ProveedorRequest(
    @NotBlank String nombre,
    String rfc,
    String telefono,
    String email,
    String direccion,
    String contactoNombre,
    Boolean activo
) {}
