package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.ProductoVarianteAtributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoVarianteAtributoRepository extends JpaRepository<ProductoVarianteAtributo, Integer> {
    List<ProductoVarianteAtributo> findByProductoVarianteIdProducto(Integer idProductoVariante);
    void deleteByProductoVarianteIdProducto(Integer idProductoVariante);
}
