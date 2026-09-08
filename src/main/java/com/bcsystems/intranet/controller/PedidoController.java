package com.bcsystems.intranet.controller;

import com.bcsystems.intranet.dto.PedidoRequest;
import com.bcsystems.intranet.dto.PedidoResponse;
import com.bcsystems.intranet.dto.RecepcionRequest;
import com.bcsystems.intranet.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PEDIDOS_VER')")
    public ResponseEntity<Page<PedidoResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idProveedor,
            @PageableDefault(size = 10, sort = "idPedido", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listar(search, estado, idProveedor, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDOS_VER')")
    public ResponseEntity<PedidoResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PEDIDOS_CREAR')")
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('PEDIDOS_CANCELAR')")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.cancelar(id));
    }

    @PostMapping("/{id}/recibir")
    @PreAuthorize("hasAuthority('PEDIDOS_RECEBIR')")
    public ResponseEntity<PedidoResponse> recibir(@PathVariable Integer id, @Valid @RequestBody RecepcionRequest request) {
        return ResponseEntity.ok(pedidoService.recibir(id, request));
    }

    @PostMapping("/{id}/completar")
    @PreAuthorize("hasAuthority('PEDIDOS_RECEBIR')")
    public ResponseEntity<PedidoResponse> completar(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.completar(id));
    }
}
