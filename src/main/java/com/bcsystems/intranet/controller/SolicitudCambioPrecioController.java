package com.bcsystems.intranet.controller;

import com.bcsystems.intranet.dto.CambioPrecioResponse;
import com.bcsystems.intranet.dto.CodigoCambioPrecioResponse;
import com.bcsystems.intranet.dto.CodigoRequest;
import com.bcsystems.intranet.dto.SolicitarCambioPrecioRequest;
import com.bcsystems.intranet.service.SolicitudCambioPrecioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes-cambio-precio")
public class SolicitudCambioPrecioController {

    private final SolicitudCambioPrecioService solicitudService;

    public SolicitudCambioPrecioController(SolicitudCambioPrecioService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('AUTORIZACIONES_VER','VENTAS_EDITAR_PRECIO')")
    public ResponseEntity<List<CambioPrecioResponse>> listar(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(solicitudService.listar(estado));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('VENTAS_CREAR','VENTAS_EDITAR_PRECIO')")
    public ResponseEntity<CambioPrecioResponse> solicitar(
            @Valid @RequestBody SolicitarCambioPrecioRequest request) {
        return ResponseEntity.ok(solicitudService.solicitar(request));
    }

    @GetMapping("/producto/{idProducto}")
    @PreAuthorize("hasAnyAuthority('VENTAS_CREAR','VENTAS_EDITAR_PRECIO','AUTORIZACIONES_VER')")
    public ResponseEntity<CambioPrecioResponse> pendienteDeProducto(
            @PathVariable Integer idProducto,
            @RequestParam(required = false) Integer idSucursal) {
        return ResponseEntity.ok(solicitudService.pendienteDeProducto(idProducto, idSucursal));
    }

    @PostMapping("/{id}/generar-codigo")
    @PreAuthorize("hasAuthority('VENTAS_EDITAR_PRECIO')")
    public ResponseEntity<CodigoCambioPrecioResponse> generarCodigo(@PathVariable Integer id) {
        return ResponseEntity.ok(solicitudService.generarCodigo(id));
    }

    @PostMapping("/{id}/validar-codigo")
    @PreAuthorize("hasAnyAuthority('VENTAS_CREAR','VENTAS_EDITAR_PRECIO')")
    public ResponseEntity<CambioPrecioResponse> validarCodigo(
            @PathVariable Integer id,
            @RequestBody(required = false) CodigoRequest request) {
        return ResponseEntity.ok(solicitudService.validarCodigo(id, request != null ? request.codigo() : null));
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAuthority('VENTAS_EDITAR_PRECIO')")
    public ResponseEntity<CambioPrecioResponse> rechazar(@PathVariable Integer id) {
        return ResponseEntity.ok(solicitudService.rechazar(id));
    }
}