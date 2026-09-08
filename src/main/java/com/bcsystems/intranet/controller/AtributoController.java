package com.bcsystems.intranet.controller;

import com.bcsystems.intranet.dto.AtributoRequest;
import com.bcsystems.intranet.dto.AtributoResponse;
import com.bcsystems.intranet.service.AtributoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atributos")
public class AtributoController {

    private final AtributoService atributoService;

    public AtributoController(AtributoService atributoService) {
        this.atributoService = atributoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ATRIBUTOS_VER')")
    public ResponseEntity<List<AtributoResponse>> listar() {
        return ResponseEntity.ok(atributoService.listar());
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAuthority('ATRIBUTOS_VER')")
    public ResponseEntity<List<AtributoResponse>> listarActivos() {
        return ResponseEntity.ok(atributoService.listarActivos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ATRIBUTOS_VER')")
    public ResponseEntity<AtributoResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(atributoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ATRIBUTOS_CREAR')")
    public ResponseEntity<AtributoResponse> crear(@Valid @RequestBody AtributoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atributoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ATRIBUTOS_EDITAR')")
    public ResponseEntity<AtributoResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody AtributoRequest request) {
        return ResponseEntity.ok(atributoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ATRIBUTOS_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        atributoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
