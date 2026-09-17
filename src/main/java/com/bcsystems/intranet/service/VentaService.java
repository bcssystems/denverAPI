package com.bcsystems.intranet.service;

import com.bcsystems.intranet.dto.VentaEsperaRequest;
import com.bcsystems.intranet.dto.VentaRequest;
import com.bcsystems.intranet.dto.VentaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {
    VentaResponse crear(VentaRequest request);
    VentaResponse obtenerPorId(Integer id);
    List<VentaResponse> listarPorCaja(Integer idCaja);
    Page<VentaResponse> listar(Integer idSucursal, Integer idCaja, String estado, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    VentaResponse cancelar(Integer id);
    VentaResponse ponerEnEspera(Integer id);
    VentaResponse actualizarEspera(Integer id, VentaEsperaRequest request);
    VentaResponse cancelarEspera(Integer id);
    VentaResponse reanudar(Integer id);
    List<VentaResponse> ventasEnEspera(Integer idCaja);
    List<VentaResponse> listarPorSucursal(Integer idSucursal);
    VentaResponse ventaRapida(Integer idCaja, String descripcion, Double precioCompra, Double precioVenta, Integer cantidad, Integer idCliente);
}
