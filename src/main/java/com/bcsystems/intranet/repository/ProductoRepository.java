package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.InventarioSucursal;
import com.bcsystems.intranet.domain.Producto;
import com.bcsystems.intranet.domain.ProductoMultimedia;
import com.bcsystems.intranet.domain.ProductoVarianteAtributo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    Optional<Producto> findBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCaseAndIdProductoNot(String sku, Integer idProducto);

    @Query("SELECT COALESCE(SUM(COALESCE(p.costoPromedio, 0) * p.stockActual), 0) FROM Producto p WHERE p.activo = true")
    Double sumCostoTotalInventario();

    @Query("SELECT p FROM Producto p WHERE p.productoPadre IS NULL " +
           "AND (:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR EXISTS (SELECT pva FROM ProductoVarianteAtributo pva " +
           "WHERE (pva.productoVariante.idProducto = p.idProducto " +
           "OR pva.productoVariante.productoPadre.idProducto = p.idProducto) " +
           "AND (LOWER(pva.valor.valor) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(pva.atributo.nombre) LIKE LOWER(CONCAT('%', :search, '%'))))) " +
           "AND (:activo IS NULL OR p.activo = :activo) " +
           "AND (:idSucursal IS NULL OR EXISTS (SELECT i FROM InventarioSucursal i WHERE i.producto.idProducto = p.idProducto AND i.sucursal.idSucursal = :idSucursal) " +
           "OR EXISTS (SELECT i2 FROM InventarioSucursal i2 WHERE i2.producto.productoPadre.idProducto = p.idProducto AND i2.sucursal.idSucursal = :idSucursal))")
    Page<Producto> buscarConFiltros(@Param("search") String search,
                                    @Param("activo") Boolean activo,
                                    @Param("idSucursal") Integer idSucursal,
                                    Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.activo = true " +
           "AND (p.tieneVariantes = false OR p.tieneVariantes IS NULL OR p.productoPadre IS NOT NULL) " +
           "AND (:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:idSucursal IS NULL OR EXISTS (SELECT i FROM InventarioSucursal i WHERE i.producto.idProducto = p.idProducto AND i.sucursal.idSucursal = :idSucursal))")
    Page<Producto> buscarParaVenta(@Param("search") String search,
                                   @Param("idSucursal") Integer idSucursal,
                                   Pageable pageable);

    List<Producto> findByProductoPadreIdProducto(Integer idProductoPadre);

    @Query("SELECT p FROM Producto p WHERE p.productoPadre.idProducto IN :ids")
    List<Producto> findByProductoPadreIdProductoIn(@Param("ids") java.util.Collection<Integer> ids);

    @Query("SELECT m FROM ProductoMultimedia m WHERE m.producto.idProducto IN :ids")
    List<ProductoMultimedia> findMultimediaByProductoIdIn(@Param("ids") java.util.Collection<Integer> ids);

    @Query("SELECT i FROM InventarioSucursal i JOIN FETCH i.sucursal WHERE i.producto.idProducto IN :ids")
    List<InventarioSucursal> findInventarioByProductoIdIn(@Param("ids") java.util.Collection<Integer> ids);

    @Query("SELECT pva FROM ProductoVarianteAtributo pva JOIN FETCH pva.atributo JOIN FETCH pva.valor " +
           "WHERE pva.productoVariante.idProducto IN :ids")
    List<ProductoVarianteAtributo> findVarianteAtributosByProductoIdIn(@Param("ids") java.util.Collection<Integer> ids);

    @Query("SELECT COALESCE(SUM(p.stockActual), 0) FROM Producto p WHERE p.productoPadre.idProducto = :idProductoPadre")
    Integer sumStockByProductoPadreId(@Param("idProductoPadre") Integer idProductoPadre);

    @Query(value = "SELECT COUNT(*) AS total, " +
                   "COALESCE(SUM(IF(p.activo, 1, 0)), 0) AS activos, " +
                   "COALESCE(SUM(p.stock_actual), 0) AS stock_actual, " +
                   "COALESCE(SUM(p.stock_minimo), 0) AS stock_minimo " +
                   "FROM producto p", nativeQuery = true)
    List<Object[]> resumenStats();

    @Modifying
    @Query(value = "DELETE FROM producto WHERE id_producto_padre = :padreId", nativeQuery = true)
    void deleteVariantsByPadreIdNative(@Param("padreId") Integer padreId);

    @Modifying
    @Query(value = "DELETE FROM inventario_sucursal WHERE id_producto IN (SELECT id_producto FROM producto WHERE id_producto_padre = :padreId)", nativeQuery = true)
    void deleteInventariosByPadreId(@Param("padreId") Integer padreId);

    @Modifying
    @Query(value = "DELETE FROM producto_variante_atributo WHERE id_producto_variante IN (SELECT id_producto FROM producto WHERE id_producto_padre = :padreId)", nativeQuery = true)
    void deleteVarianteAtributosByPadreId(@Param("padreId") Integer padreId);

    @Modifying
    @Query(value = "DELETE FROM movimiento_stock WHERE id_producto IN (SELECT id_producto FROM producto WHERE id_producto_padre = :padreId)", nativeQuery = true)
    void deleteMovimientosByPadreId(@Param("padreId") Integer padreId);
}
