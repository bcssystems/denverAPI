package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.SolicitudCambioPrecio;
import com.bcsystems.intranet.domain.en.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudCambioPrecioRepository extends JpaRepository<SolicitudCambioPrecio, Integer> {
    Optional<SolicitudCambioPrecio> findFirstByProductoIdProductoAndSucursalIdSucursalAndEstadoOrderByFechaSolicitudDesc(
            Integer idProducto, Integer idSucursal, EstadoSolicitud estado);
    List<SolicitudCambioPrecio> findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud estado);
    List<SolicitudCambioPrecio> findAllByOrderByFechaSolicitudDesc();
}