package com.bcsystems.intranet.service;

import com.bcsystems.intranet.dto.MovimientoStockRequest;
import com.bcsystems.intranet.dto.ProductoRequest;
import com.bcsystems.intranet.dto.ProductoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProductoService {
    Page<ProductoResponse> listar(String search, Boolean activo, Integer idSucursal, Pageable pageable);
    ProductoResponse obtenerPorId(Integer id);
    ProductoResponse crear(ProductoRequest request);
    ProductoResponse actualizar(Integer id, ProductoRequest request);
    void eliminar(Integer id);
    ProductoResponse agregarMultimedia(Integer idProducto, MultipartFile archivo, Boolean esPrincipal);
    void eliminarMultimedia(Integer idMultimedia);
    ProductoResponse marcarMultimediaPrincipal(Integer idProducto, Integer idMultimedia);
    ProductoResponse actualizarStockSucursal(Integer idProducto, Integer idSucursal, Integer nuevoStock);
    ProductoResponse registrarMovimientoStock(Integer idProducto, MovimientoStockRequest request);
    ProductoStats obtenerStats();

    record ProductoStats(long total, long activos, long stockGlobal, long stockMinimo) {}
}
