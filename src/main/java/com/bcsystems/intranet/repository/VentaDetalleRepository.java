package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Integer> {
    List<VentaDetalle> findByVentaIdVenta(Integer idVenta);
}
