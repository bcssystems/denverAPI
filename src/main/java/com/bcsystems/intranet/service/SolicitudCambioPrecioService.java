package com.bcsystems.intranet.service;

import com.bcsystems.intranet.dto.CambioPrecioResponse;
import com.bcsystems.intranet.dto.CodigoCambioPrecioResponse;
import com.bcsystems.intranet.dto.SolicitarCambioPrecioRequest;

import java.util.List;

public interface SolicitudCambioPrecioService {
    CambioPrecioResponse solicitar(SolicitarCambioPrecioRequest request);
    CambioPrecioResponse pendienteDeProducto(Integer idProducto, Integer idSucursal);
    List<CambioPrecioResponse> listar(String estado);
    CodigoCambioPrecioResponse generarCodigo(Integer idSolicitud);
    CambioPrecioResponse validarCodigo(Integer idSolicitud, String codigo);
    CambioPrecioResponse rechazar(Integer idSolicitud);
}