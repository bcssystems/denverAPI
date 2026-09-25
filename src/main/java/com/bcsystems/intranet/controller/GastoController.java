package com.bcsystems.intranet.controller;

import com.bcsystems.intranet.dto.GastoRequest;
import com.bcsystems.intranet.dto.GastoResponse;
import com.bcsystems.intranet.service.GastoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gastos")
public class GastoController {

    private final GastoService gastoService;

    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('GASTOS_CREAR','GASTOS_VER')")
    public ResponseEntity<GastoResponse> solicitar(@Valid @RequestBody GastoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gastoService.solicitar(request));
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAuthority('GASTOS_VER')")
    public ResponseEntity<List<GastoResponse>> pendientes() {
        return ResponseEntity.ok(gastoService.pendientes());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAuthority('GASTOS_VER')")
    public ResponseEntity<List<GastoResponse>> listarTodos() {
        return ResponseEntity.ok(gastoService.listarTodos());
    }

    @GetMapping("/caja/{idCaja}")
    @PreAuthorize("hasAuthority('GASTOS_VER')")
    public ResponseEntity<List<GastoResponse>> listarPorCaja(@PathVariable Integer idCaja) {
        return ResponseEntity.ok(gastoService.listarPorCaja(idCaja));
    }

    @GetMapping("/pendientes/{idCaja}/count")
    @PreAuthorize("hasAuthority('GASTOS_VER')")
    public ResponseEntity<Long> contarPendientesPorCaja(@PathVariable Integer idCaja) {
        return ResponseEntity.ok(gastoService.contarPendientesPorCaja(idCaja));
    }

    @PostMapping("/{idGasto}/autorizar")
    @PreAuthorize("hasAuthority('GASTOS_AUTORIZAR')")
    public ResponseEntity<GastoResponse> autorizar(@PathVariable Integer idGasto) {
        return ResponseEntity.ok(gastoService.autorizar(idGasto));
    }

    @PostMapping("/{idGasto}/rechazar")
    @PreAuthorize("hasAuthority('GASTOS_AUTORIZAR')")
    public ResponseEntity<GastoResponse> rechazar(@PathVariable Integer idGasto) {
        return ResponseEntity.ok(gastoService.rechazar(idGasto));
    }
}
