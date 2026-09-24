package com.bcsystems.intranet.service.impl;

import com.bcsystems.intranet.domain.Persona;
import com.bcsystems.intranet.domain.SolicitudCancelacion;
import com.bcsystems.intranet.domain.Venta;
import com.bcsystems.intranet.domain.en.EstadoSolicitud;
import com.bcsystems.intranet.domain.en.EstadoVenta;
import com.bcsystems.intranet.dto.CodigoAutorizacionResponse;
import com.bcsystems.intranet.dto.SolicitudCancelacionResponse;
import com.bcsystems.intranet.exception.InvalidEntryException;
import com.bcsystems.intranet.exception.NotFoundException;
import com.bcsystems.intranet.repository.PersonaRepository;
import com.bcsystems.intranet.repository.SolicitudCancelacionRepository;
import com.bcsystems.intranet.repository.VentaRepository;
import com.bcsystems.intranet.service.AuditoriaService;
import com.bcsystems.intranet.service.SolicitudCancelacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SolicitudCancelacionServiceImpl implements SolicitudCancelacionService {

    private final SolicitudCancelacionRepository solicitudRepository;
    private final VentaRepository ventaRepository;
    private final PersonaRepository personaRepository;
    private final AuditoriaService auditoriaService;

    @Override
    @Transactional
    public SolicitudCancelacionResponse solicitar(Integer idVenta, String motivo) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new InvalidEntryException("La venta ya está cancelada");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new InvalidEntryException("El motivo es obligatorio para solicitar la cancelación");
        }

        solicitudRepository.findFirstByVentaIdVentaAndEstadoOrderByFechaSolicitudDesc(idVenta, EstadoSolicitud.PENDIENTE)
                .ifPresent(s -> {
                    throw new InvalidEntryException("Ya existe una solicitud de cancelación pendiente para esta venta");
                });

        SolicitudCancelacion solicitud = SolicitudCancelacion.builder()
                .venta(venta)
                .motivo(motivo.trim())
                .solicitante(obtenerPersonaActual())
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                .build();
        solicitud = solicitudRepository.save(solicitud);

        auditoriaService.registrar("SOLICITUD_CANCELACION", solicitud.getIdSolicitud(), "CREACION",
                obtenerPersonaActual().getUsuario(),
                "Solicitud de cancelación de venta #" + idVenta + ": " + solicitud.getMotivo());

        return toResponse(solicitud);
    }

    @Override
    public SolicitudCancelacionResponse solicitudPendienteDeVenta(Integer idVenta) {
        return solicitudRepository
                .findFirstByVentaIdVentaAndEstadoOrderByFechaSolicitudDesc(idVenta, EstadoSolicitud.PENDIENTE)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    public List<SolicitudCancelacionResponse> listar(String estado) {
        List<SolicitudCancelacion> list = (estado == null || estado.isBlank())
                ? solicitudRepository.findAllByOrderByFechaSolicitudDesc()
                : solicitudRepository.findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud.valueOf(estado));
        return list.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CodigoAutorizacionResponse generarCodigo(Integer idSolicitud) {
        SolicitudCancelacion solicitud = buscarPendiente(idSolicitud);
        Persona admin = obtenerPersonaActual();

        String codigo = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        solicitud.setCodigo(codigo);
        solicitud.setGeneradoPor(admin);
        solicitud.setFechaGeneracionCodigo(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        auditoriaService.registrar("SOLICITUD_CANCELACION", solicitud.getIdSolicitud(), "ACTUALIZACION",
                admin.getUsuario(), "Código de autorización generado para la venta #" + solicitud.getVenta().getIdVenta());

        return new CodigoAutorizacionResponse(
                solicitud.getIdSolicitud(),
                solicitud.getVenta().getIdVenta(),
                codigo,
                solicitud.getFechaGeneracionCodigo(),
                solicitud.getFechaGeneracionCodigo().plusMinutes(10));
    }

    @Override
    @Transactional
    public SolicitudCancelacionResponse rechazar(Integer idSolicitud) {
        SolicitudCancelacion solicitud = buscarPendiente(idSolicitud);
        Persona autorizador = obtenerPersonaActual();

        solicitud.setEstado(EstadoSolicitud.RECHAZADO);
        solicitud.setAutorizador(autorizador);
        solicitud.setFechaAutorizacion(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        auditoriaService.registrar("SOLICITUD_CANCELACION", solicitud.getIdSolicitud(), "ACTUALIZACION",
                autorizador.getUsuario(),
                "Solicitud de cancelación rechazada para la venta #" + solicitud.getVenta().getIdVenta());

        return toResponse(solicitud);
    }

    private SolicitudCancelacion buscarPendiente(Integer idSolicitud) {
        SolicitudCancelacion solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new InvalidEntryException("La solicitud no está pendiente");
        }
        return solicitud;
    }

    private Persona obtenerPersonaActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return personaRepository.findByUsuario(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private SolicitudCancelacionResponse toResponse(SolicitudCancelacion s) {
        Venta venta = s.getVenta();
        return new SolicitudCancelacionResponse(
                s.getIdSolicitud(),
                venta.getIdVenta(),
                venta.getCaja().getSucursal().getIdSucursal(),
                venta.getCaja().getSucursal().getNombre(),
                venta.getCliente() != null ? venta.getCliente().getNombre() + " " + venta.getCliente().getApellidoPaterno() : "Mostrador",
                s.getSolicitante().getUsuario(),
                venta.getTotal(),
                s.getMotivo(),
                s.getEstado().name(),
                s.getCodigo() != null,
                s.getFechaSolicitud(),
                s.getGeneradoPor() != null ? s.getGeneradoPor().getUsuario() : null,
                s.getAutorizador() != null ? s.getAutorizador().getUsuario() : null,
                s.getFechaAutorizacion());
    }
}