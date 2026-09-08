package com.bcsystems.intranet.dto;

import java.util.List;

public record PermisoResponse(
    Integer idPermiso,
    String clave,
    String nombre,
    String descripcion,
    String modulo
) {}
