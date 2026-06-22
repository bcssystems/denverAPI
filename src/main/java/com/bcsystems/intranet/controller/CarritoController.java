package com.bcsystems.intranet.controller;

import com.bcsystems.intranet.dto.ReservaProductoResponse;
import com.bcsystems.intranet.service.ReservaProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carrito")
public class CarritoController {

    private final ReservaProductoService reservaProductoService;

    public CarritoController(ReservaProductoService reservaProductoService) {
        this.reservaProductoService = reservaProductoService;
    }

    @PostMapping("/agregar")
    public ResponseEntity<Map<String, Object>> agregar(@RequestBody Map<String, Object> body) {
        Integer idCaja = Integer.valueOf(body.get("idCaja").toString());
        Integer idProducto = Integer.valueOf(body.get("idProducto").toString());
        Integer cantidad = body.get("cantidad") != null
                ? Integer.valueOf(body.get("cantidad").toString()) : 1;
        reservaProductoService.reservar(idCaja, idProducto, cantidad);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true));
    }

    @DeleteMapping("/quitar/{idProducto}")
    public ResponseEntity<Void> quitar(@PathVariable Integer idProducto,
                                        @RequestParam Integer idCaja) {
        reservaProductoService.quitarReserva(idCaja, idProducto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/actualizar")
    public ResponseEntity<Map<String, Object>> actualizar(@RequestBody Map<String, Object> body) {
        Integer idCaja = Integer.valueOf(body.get("idCaja").toString());
        Integer idProducto = Integer.valueOf(body.get("idProducto").toString());
        Integer cantidad = Integer.valueOf(body.get("cantidad").toString());
        reservaProductoService.actualizarCantidad(idCaja, idProducto, cantidad);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/limpiar")
    public ResponseEntity<Void> limpiar(@RequestParam Integer idCaja) {
        reservaProductoService.limpiarReservas(idCaja);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservados/{idSucursal}")
    public ResponseEntity<List<ReservaProductoResponse>> reservados(
            @PathVariable Integer idSucursal) {
        return ResponseEntity.ok(
                reservaProductoService.obtenerReservasPorSucursal(idSucursal));
    }
}
