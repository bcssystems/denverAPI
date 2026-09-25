package com.bcsystems.intranet.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductoListaResponse(
    Integer idProducto,
    String sku,
    String nombre,
    String descripcion,
    Double precio1,
    Double precio2,
    Double precio3,
    Double precio4,
    Boolean precioPersonalizado,
    Integer stockActual,
    Integer stockMinimo,
    Integer stockMaximo,
    Double costoPromedio,
    Boolean tieneVariantes,
    Integer idProductoPadre,
    Boolean activo,
    String imagenUrl,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion,
    List<MultimediaResponse> multimedia,
    List<InventarioSucursalResponse> inventarioSucursales,
    List<ProductoListaResponse> variantes,
    List<VarianteAtributoResponse> atributosAsignados
) {
    public record MultimediaResponse(Integer idMultimedia, String tipo, String url, String nombreArchivo, Boolean esPrincipal) {}
    public record InventarioSucursalResponse(Integer id, Integer idSucursal, String sucursalNombre, Integer stock, Integer stockMinimo, Integer stockMaximo) {}
    public record VarianteAtributoResponse(Integer idAtributo, String nombreAtributo, Integer idValor, String nombreValor, String codigoSku) {}
}