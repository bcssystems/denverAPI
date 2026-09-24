package com.bcsystems.intranet.service;

import com.bcsystems.intranet.dto.CodigoAutorizacionResponse;
import com.bcsystems.intranet.dto.SolicitudCancelacionResponse;

import java.util.List;

public interface SolicitudCancelacionService {
    SolicitudCancelacionResponse solicitar(Integer idVenta, String motivo);
    SolicitudCancelacionResponse solicitudPendienteDeVenta(Integer idVenta);
    List<SolicitudCancelacionResponse> listar(String estado);
    CodigoAutorizacionResponse generarCodigo(Integer idSolicitud);
    SolicitudCancelacionResponse rechazar(Integer idSolicitud);
}