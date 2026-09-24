package com.bcsystems.intranet.dto;

import java.util.List;

public record ProductoVentaResponse(
    Integer idProducto,
    String sku,
    String nombre,
    Double precio1,
    Double precio2,
    Double precio3,
    Double precio4,
    Integer stockActual,
    Double costoPromedio,
    Boolean tieneVariantes,
    Integer idProductoPadre,
    Boolean activo,
    List<InventarioSucursalResponse> inventarioSucursales,
    List<AtributoInfo> atributos
) {
    public record InventarioSucursalResponse(Integer id, Integer idSucursal, String sucursalNombre, Integer stock, Integer stockMinimo, Integer stockMaximo) {}
    public record AtributoInfo(String nombreAtributo, String nombreValor) {}
}
