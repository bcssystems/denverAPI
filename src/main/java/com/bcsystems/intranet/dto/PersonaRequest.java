package com.bcsystems.intranet.dto;

import com.bcsystems.intranet.domain.en.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PersonaRequest(
    @NotBlank String nombre,
    @NotBlank String apellido,
    @NotBlank String usuario,
    String password,
    @NotNull Rol rol,
    Boolean activa
) {}
