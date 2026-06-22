package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.VentaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaPagoRepository extends JpaRepository<VentaPago, Integer> {
    List<VentaPago> findByVentaIdVenta(Integer idVenta);
}
