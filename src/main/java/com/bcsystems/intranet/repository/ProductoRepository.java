package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    Optional<Producto> findBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCaseAndIdProductoNot(String sku, Integer idProducto);
    long countByActivoTrue();

    @Query("SELECT COALESCE(SUM(p.stockActual), 0) FROM Producto p")
    Integer sumStockActual();

    @Query("SELECT COALESCE(SUM(p.stockMinimo), 0) FROM Producto p")
    Integer sumStockMinimo();

    @Query("SELECT p FROM Producto p WHERE " +
           "(:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:activo IS NULL OR p.activo = :activo) " +
           "AND (:idSucursal IS NULL OR EXISTS (SELECT i FROM InventarioSucursal i WHERE i.producto.idProducto = p.idProducto AND i.sucursal.idSucursal = :idSucursal))")
    Page<Producto> buscarConFiltros(@Param("search") String search,
                                    @Param("activo") Boolean activo,
                                    @Param("idSucursal") Integer idSucursal,
                                    Pageable pageable);
}
