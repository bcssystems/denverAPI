package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfiguracionRequest(
        @NotBlank String clave,
        @NotBlank String valor,
        String descripcion
) {}