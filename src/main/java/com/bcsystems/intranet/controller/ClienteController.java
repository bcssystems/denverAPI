package com.bcsystems.intranet.controller;

import com.bcsystems.intranet.dto.ClienteRequest;
import com.bcsystems.intranet.dto.ClienteResponse;
import com.bcsystems.intranet.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('CLIENTES_VER')")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(clienteService.listar(null, 0, 1));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENTES_VER')")
    public ResponseEntity<Page<ClienteResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clienteService.listar(search, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTES_VER')")
    public ResponseEntity<ClienteResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENTES_CREAR')")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTES_EDITAR')")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTES_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
