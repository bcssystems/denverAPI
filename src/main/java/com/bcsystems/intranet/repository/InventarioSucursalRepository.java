package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.InventarioSucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioSucursalRepository extends JpaRepository<InventarioSucursal, Integer> {
    List<InventarioSucursal> findByProductoIdProducto(Integer idProducto);
    Optional<InventarioSucursal> findByProductoIdProductoAndSucursalIdSucursal(Integer idProducto, Integer idSucursal);
}
