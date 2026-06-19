package com.bcsystems.intranet.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String usuario,
        @NotBlank String password
) {}
