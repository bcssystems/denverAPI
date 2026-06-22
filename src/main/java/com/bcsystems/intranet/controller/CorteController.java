package com.bcsystems.intranet.controller;

import com.bcsystems.intranet.domain.CorteCaja;
import com.bcsystems.intranet.dto.CorteDetallePagoDto;
import com.bcsystems.intranet.dto.CorteResponse;
import com.bcsystems.intranet.repository.CorteCajaRepository;
import com.bcsystems.intranet.repository.CorteDetallePagoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cortes")
public class CorteController {

    private final CorteCajaRepository corteCajaRepository;
    private final CorteDetallePagoRepository corteDetallePagoRepository;

    public CorteController(CorteCajaRepository corteCajaRepository,
                           CorteDetallePagoRepository corteDetallePagoRepository) {
        this.corteCajaRepository = corteCajaRepository;
        this.corteDetallePagoRepository = corteDetallePagoRepository;
    }

    @GetMapping
    public ResponseEntity<Page<CorteResponse>> listar(
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) Integer idCaja,
            @RequestParam(required = false) LocalDateTime fechaInicio,
            @RequestParam(required = false) LocalDateTime fechaFin,
            @PageableDefault(size = 20, sort = "fechaCierre", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CorteCaja> page = corteCajaRepository.listarConFiltros(idSucursal, idCaja, fechaInicio, fechaFin, pageable);
        return ResponseEntity.ok(page.map(this::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorteResponse> obtenerPorId(@PathVariable Integer id) {
        CorteCaja corte = corteCajaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corte no encontrado con id: " + id));
        return ResponseEntity.ok(toResponse(corte));
    }

    private CorteResponse toResponse(CorteCaja c) {
        List<CorteDetallePagoDto> detallePagos = corteDetallePagoRepository.findByCorteIdCorte(c.getIdCorte())
                .stream().map(d -> new CorteDetallePagoDto(
                        d.getTipoPago().getIdTipoPago(),
                        d.getTipoPago().getNombre(),
                        d.getMonto()))
                .toList();
        return new CorteResponse(
                c.getIdCorte(), c.getCaja().getIdCaja(), c.getCaja().getNombre(),
                c.getCaja().getSucursal().getIdSucursal(), c.getCaja().getSucursal().getNombre(),
                c.getSaldoInicial(), c.getTotalVentas(),
                c.getTotalVentasContado(), c.getTotalVentasCredito(),
                c.getTotalIngresos(), c.getTotalEgresos(),
                c.getSaldoFinalContado(), null,
                c.getFechaApertura(), c.getFechaCierre(),
                c.getUsuario().getUsuario(), detallePagos);
    }
}
