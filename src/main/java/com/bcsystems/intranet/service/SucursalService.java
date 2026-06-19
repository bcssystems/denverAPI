package com.bcsystems.intranet.service;

import com.bcsystems.intranet.dto.SucursalRequest;
import com.bcsystems.intranet.dto.SucursalResponse;

import java.util.List;

public interface SucursalService {
    List<SucursalResponse> listarTodas();
    SucursalResponse obtenerPorId(Integer id);
    SucursalResponse crear(SucursalRequest request);
    SucursalResponse actualizar(Integer id, SucursalRequest request);
    void eliminar(Integer id);
}
