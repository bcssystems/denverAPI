package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.Gasto;
import com.bcsystems.intranet.domain.en.EstadoGasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Integer> {
    List<Gasto> findByCajaIdCajaOrderByFechaCreacionDesc(Integer idCaja);
    List<Gasto> findByEstadoOrderByFechaCreacionDesc(EstadoGasto estado);
    long countByCajaIdCajaAndEstado(Integer idCaja, EstadoGasto estado);
    List<Gasto> findAllByOrderByFechaCreacionDesc();
}
