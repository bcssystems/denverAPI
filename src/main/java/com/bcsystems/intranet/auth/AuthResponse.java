package com.bcsystems.intranet.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String usuario,
        String nombre,
        String rol
) {}
