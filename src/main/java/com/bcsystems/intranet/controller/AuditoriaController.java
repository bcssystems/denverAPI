package com.bcsystems.intranet.controller;

import com.bcsystems.intranet.dto.AuditoriaResponse;
import com.bcsystems.intranet.service.AuditoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auditorias")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<Page<AuditoriaResponse>> listar(
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String usuario,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditoriaService.listar(entidad, usuario, pageable));
    }
}
