package com.bcsystems.intranet.service.impl;

import com.bcsystems.intranet.domain.Persona;
import com.bcsystems.intranet.domain.Producto;
import com.bcsystems.intranet.domain.SolicitudCambioPrecio;
import com.bcsystems.intranet.domain.Sucursal;
import com.bcsystems.intranet.domain.en.EstadoSolicitud;
import com.bcsystems.intranet.dto.CambioPrecioResponse;
import com.bcsystems.intranet.dto.CodigoCambioPrecioResponse;
import com.bcsystems.intranet.dto.SolicitarCambioPrecioRequest;
import com.bcsystems.intranet.exception.InvalidEntryException;
import com.bcsystems.intranet.exception.NotFoundException;
import com.bcsystems.intranet.repository.PersonaRepository;
import com.bcsystems.intranet.repository.ProductoRepository;
import com.bcsystems.intranet.repository.SolicitudCambioPrecioRepository;
import com.bcsystems.intranet.repository.SucursalRepository;
import com.bcsystems.intranet.service.AuditoriaService;
import com.bcsystems.intranet.service.SolicitudCambioPrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SolicitudCambioPrecioServiceImpl implements SolicitudCambioPrecioService {

    private final SolicitudCambioPrecioRepository solicitudRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    private final PersonaRepository personaRepository;
    private final AuditoriaService auditoriaService;

    @Override
    @Transactional
    public CambioPrecioResponse solicitar(SolicitarCambioPrecioRequest request) {
        Producto producto = productoRepository.findById(request.idProducto())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        Sucursal sucursal = sucursalRepository.findById(request.idSucursal())
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        if (request.nuevoPrecio() == null || request.nuevoPrecio() <= 0) {
            throw new InvalidEntryException("El nuevo precio debe ser mayor a cero");
        }
        if (request.motivo() == null || request.motivo().isBlank()) {
            throw new InvalidEntryException("El motivo es obligatorio para solicitar el cambio de precio");
        }

        solicitudRepository.findFirstByProductoIdProductoAndSucursalIdSucursalAndEstadoOrderByFechaSolicitudDesc(
                request.idProducto(), request.idSucursal(), EstadoSolicitud.PENDIENTE)
                .ifPresent(s -> {
                    throw new InvalidEntryException("Ya existe una solicitud de cambio de precio pendiente para este producto");
                });

        Persona solicitante = obtenerPersonaActual();
        SolicitudCambioPrecio solicitud = SolicitudCambioPrecio.builder()
                .producto(producto)
                .sucursal(sucursal)
                .precioActual(request.precioActual())
                .nuevoPrecio(request.nuevoPrecio())
                .motivo(request.motivo().trim())
                .solicitante(solicitante)
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                .build();
        solicitud = solicitudRepository.save(solicitud);

        auditoriaService.registrar("SOLICITUD_CAMBIO_PRECIO", solicitud.getIdSolicitud(), "CREACION",
                solicitante.getUsuario(),
                "Solicitud de cambio de precio de " + producto.getNombre() + ": $" + request.precioActual()
                        + " -> $" + request.nuevoPrecio() + " (" + solicitud.getMotivo() + ")");

        return toResponse(solicitud);
    }

    @Override
    public CambioPrecioResponse pendienteDeProducto(Integer idProducto, Integer idSucursal) {
        return solicitudRepository
                .findFirstByProductoIdProductoAndSucursalIdSucursalAndEstadoOrderByFechaSolicitudDesc(
                        idProducto, idSucursal, EstadoSolicitud.PENDIENTE)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    public List<CambioPrecioResponse> listar(String estado) {
        List<SolicitudCambioPrecio> list = (estado == null || estado.isBlank())
                ? solicitudRepository.findAllByOrderByFechaSolicitudDesc()
                : solicitudRepository.findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud.valueOf(estado));
        return list.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CodigoCambioPrecioResponse generarCodigo(Integer idSolicitud) {
        SolicitudCambioPrecio solicitud = buscarPendiente(idSolicitud);
        Persona admin = obtenerPersonaActual();

        String codigo = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        solicitud.setCodigo(codigo);
        solicitud.setGeneradoPor(admin);
        solicitud.setFechaGeneracionCodigo(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        auditoriaService.registrar("SOLICITUD_CAMBIO_PRECIO", solicitud.getIdSolicitud(), "ACTUALIZACION",
                admin.getUsuario(), "Código de autorización generado para cambio de precio de "
                        + solicitud.getProducto().getNombre());

        return new CodigoCambioPrecioResponse(
                solicitud.getIdSolicitud(),
                solicitud.getProducto().getIdProducto(),
                codigo,
                solicitud.getFechaGeneracionCodigo(),
                solicitud.getFechaGeneracionCodigo().plusMinutes(10));
    }

    @Override
    @Transactional
    public CambioPrecioResponse validarCodigo(Integer idSolicitud, String codigo) {
        SolicitudCambioPrecio solicitud = buscarPendiente(idSolicitud);

        if (codigo == null || codigo.isBlank()) {
            throw new InvalidEntryException("Ingresa el código de autorización del administrador");
        }
        if (!codigo.trim().equals(solicitud.getCodigo())) {
            throw new InvalidEntryException("Código de autorización inválido");
        }
        if (solicitud.getFechaGeneracionCodigo() == null
                || solicitud.getFechaGeneracionCodigo().plusMinutes(10).isBefore(LocalDateTime.now())) {
            solicitud.setEstado(EstadoSolicitud.EXPIRADA);
            solicitudRepository.save(solicitud);
            throw new InvalidEntryException("El código de autorización expiró. Pide uno nuevo al administrador");
        }

        Persona autorizador = obtenerPersonaActual();
        solicitud.setEstado(EstadoSolicitud.AUTORIZADO);
        solicitud.setAutorizador(autorizador);
        solicitud.setFechaAutorizacion(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        auditoriaService.registrar("SOLICITUD_CAMBIO_PRECIO", solicitud.getIdSolicitud(), "ACTUALIZACION",
                autorizador.getUsuario(),
                "Cambio de precio autorizado para " + solicitud.getProducto().getNombre()
                        + " ($" + solicitud.getPrecioActual() + " -> $" + solicitud.getNuevoPrecio() + ")");

        return toResponse(solicitud);
    }

    @Override
    @Transactional
    public CambioPrecioResponse rechazar(Integer idSolicitud) {
        SolicitudCambioPrecio solicitud = buscarPendiente(idSolicitud);
        Persona autorizador = obtenerPersonaActual();

        solicitud.setEstado(EstadoSolicitud.RECHAZADO);
        solicitud.setAutorizador(autorizador);
        solicitud.setFechaAutorizacion(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        auditoriaService.registrar("SOLICITUD_CAMBIO_PRECIO", solicitud.getIdSolicitud(), "ACTUALIZACION",
                autorizador.getUsuario(),
                "Solicitud de cambio de precio rechazada para " + solicitud.getProducto().getNombre());

        return toResponse(solicitud);
    }

    private SolicitudCambioPrecio buscarPendiente(Integer idSolicitud) {
        SolicitudCambioPrecio solicitud = solicitudRepository.findById(idSolicitud)
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

    private CambioPrecioResponse toResponse(SolicitudCambioPrecio s) {
        return new CambioPrecioResponse(
                s.getIdSolicitud(),
                s.getProducto().getIdProducto(),
                s.getProducto().getSku(),
                s.getProducto().getNombre(),
                s.getSucursal().getIdSucursal(),
                s.getSucursal().getNombre(),
                s.getPrecioActual(),
                s.getNuevoPrecio(),
                s.getMotivo(),
                s.getEstado().name(),
                s.getCodigo() != null,
                s.getFechaSolicitud(),
                s.getSolicitante().getUsuario(),
                s.getGeneradoPor() != null ? s.getGeneradoPor().getUsuario() : null,
                s.getAutorizador() != null ? s.getAutorizador().getUsuario() : null,
                s.getFechaAutorizacion());
    }
}