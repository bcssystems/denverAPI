package com.bcsystems.intranet.controller;

import com.bcsystems.intranet.dto.CodigoAutorizacionResponse;
import com.bcsystems.intranet.dto.SolicitudCancelacionRequest;
import com.bcsystems.intranet.dto.SolicitudCancelacionResponse;
import com.bcsystems.intranet.service.SolicitudCancelacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes-cancelacion")
public class SolicitudCancelacionController {

    private final SolicitudCancelacionService solicitudCancelacionService;

    public SolicitudCancelacionController(SolicitudCancelacionService solicitudCancelacionService) {
        this.solicitudCancelacionService = solicitudCancelacionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('AUTORIZACIONES_VER','VENTAS_CANCELAR')")
    public ResponseEntity<List<SolicitudCancelacionResponse>> listar(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(solicitudCancelacionService.listar(estado));
    }

    @PostMapping("/venta/{idVenta}")
    @PreAuthorize("hasAnyAuthority('VENTAS_CREAR','VENTAS_CANCELAR')")
    public ResponseEntity<SolicitudCancelacionResponse> solicitar(@PathVariable Integer idVenta,
            @RequestBody(required = false) SolicitudCancelacionRequest request) {
        return ResponseEntity.ok(solicitudCancelacionService.solicitar(idVenta, request.motivo()));
    }

    @GetMapping("/venta/{idVenta}")
    @PreAuthorize("hasAnyAuthority('VENTAS_CREAR','VENTAS_CANCELAR','AUTORIZACIONES_VER')")
    public ResponseEntity<SolicitudCancelacionResponse> solicitudPendienteDeVenta(@PathVariable Integer idVenta) {
        return ResponseEntity.ok(solicitudCancelacionService.solicitudPendienteDeVenta(idVenta));
    }

    @PostMapping("/{id}/generar-codigo")
    @PreAuthorize("hasAuthority('VENTAS_CANCELAR')")
    public ResponseEntity<CodigoAutorizacionResponse> generarCodigo(@PathVariable Integer id) {
        return ResponseEntity.ok(solicitudCancelacionService.generarCodigo(id));
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAuthority('VENTAS_CANCELAR')")
    public ResponseEntity<SolicitudCancelacionResponse> rechazar(@PathVariable Integer id) {
        return ResponseEntity.ok(solicitudCancelacionService.rechazar(id));
    }
}