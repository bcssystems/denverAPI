package com.bcsystems.intranet.service;

import com.bcsystems.intranet.dto.VentaRequest;
import com.bcsystems.intranet.dto.VentaResponse;

import java.util.List;

public interface VentaService {
    VentaResponse crear(VentaRequest request);
    VentaResponse obtenerPorId(Integer id);
    List<VentaResponse> listarPorCaja(Integer idCaja);
    VentaResponse cancelar(Integer id);
    VentaResponse ponerEnEspera(Integer id);
    VentaResponse reanudar(Integer id);
    List<VentaResponse> ventasEnEspera(Integer idCaja);
    VentaResponse ventaRapida(Integer idCaja, String descripcion, Double precioCompra, Double precioVenta, Integer cantidad, Integer idCliente);
}
