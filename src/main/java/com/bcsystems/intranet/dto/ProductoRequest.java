package com.bcsystems.intranet.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ProductoRequest(
    @NotBlank String sku,
    @NotBlank String nombre,
    String descripcion,
    Double precio1,
    Double precio2,
    Double precio3,
    Double precio4,
    String material,
    String tipoMolde,
    String talla,
    String accesorio1,
    String accesorio2,
    Boolean activo,
    List<InventarioSucursalRequest> inventarios
) {}
