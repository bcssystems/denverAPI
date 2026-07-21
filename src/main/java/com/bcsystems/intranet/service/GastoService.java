package com.bcsystems.intranet.service;

import com.bcsystems.intranet.dto.GastoRequest;
import com.bcsystems.intranet.dto.GastoResponse;

import java.util.List;

public interface GastoService {
    GastoResponse solicitar(GastoRequest request);
    List<GastoResponse> pendientes();
    List<GastoResponse> listarPorCaja(Integer idCaja);
    long contarPendientesPorCaja(Integer idCaja);
    GastoResponse autorizar(Integer idGasto);
    GastoResponse rechazar(Integer idGasto);
}
