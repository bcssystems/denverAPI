package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotBlank;

public record TipoPagoRequest(
        @NotBlank String nombre
) {}
