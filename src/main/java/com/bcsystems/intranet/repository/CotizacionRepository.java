package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.Cotizacion;
import com.bcsystems.intranet.domain.en.EstadoCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Integer> {
    List<Cotizacion> findAllByOrderByFechaCreacionDesc();
    List<Cotizacion> findByEstadoOrderByFechaCreacionDesc(EstadoCotizacion estado);
    List<Cotizacion> findByClienteIdClienteOrderByFechaCreacionDesc(Integer idCliente);
}
