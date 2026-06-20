package com.bcsystems.intranet.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductoResponse(
    Integer idProducto,
    String sku,
    String nombre,
    String descripcion,
    Double precio1,
    Double precio2,
    Double precio3,
    Double precio4,
    Integer stockActual,
    Integer stockMinimo,
    Integer stockMaximo,
    String material,
    String numeroMolde,
    String talla,
    String accesorio1,
    String accesorio2,
    Boolean activo,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion,
    List<MultimediaResponse> multimedia,
    List<InventarioSucursalResponse> inventarioSucursales
) {
    public record MultimediaResponse(Integer idMultimedia, String tipo, String url, String nombreArchivo, Boolean esPrincipal) {}
    public record InventarioSucursalResponse(Integer id, Integer idSucursal, String sucursalNombre, Integer stock) {}
}
