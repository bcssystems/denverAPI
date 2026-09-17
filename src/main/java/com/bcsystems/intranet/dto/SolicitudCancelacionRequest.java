package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudCancelacionRequest(
        @NotBlank String motivo
) {}