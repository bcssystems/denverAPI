package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.Venta;
import com.bcsystems.intranet.domain.en.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    List<Venta> findByCajaIdCajaAndEstadoOrderByFechaDesc(Integer idCaja, EstadoVenta estado);
    List<Venta> findByCajaIdCajaAndEstadoAndFechaBetweenOrderByFechaDesc(
            Integer idCaja, EstadoVenta estado, LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findByCajaIdCajaAndFechaBetweenOrderByFechaDesc(
            Integer idCaja, LocalDateTime inicio, LocalDateTime fin);
}
