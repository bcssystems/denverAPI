package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.SolicitudCancelacion;
import com.bcsystems.intranet.domain.en.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudCancelacionRepository extends JpaRepository<SolicitudCancelacion, Integer> {
    Optional<SolicitudCancelacion> findFirstByVentaIdVentaAndEstadoOrderByFechaSolicitudDesc(
            Integer idVenta, EstadoSolicitud estado);
    List<SolicitudCancelacion> findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud estado);
    List<SolicitudCancelacion> findAllByOrderByFechaSolicitudDesc();
}