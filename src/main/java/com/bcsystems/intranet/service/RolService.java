package com.bcsystems.intranet.service;

import com.bcsystems.intranet.dto.PermisoResponse;
import com.bcsystems.intranet.dto.RolRequest;
import com.bcsystems.intranet.dto.RolResponse;

import java.util.List;
import java.util.Map;

public interface RolService {
    List<RolResponse> listar();
    RolResponse obtenerPorId(Integer id);
    RolResponse crear(RolRequest request);
    RolResponse actualizar(Integer id, RolRequest request);
    void eliminar(Integer id);
    RolResponse reactivar(Integer id);
    Map<String, List<PermisoResponse>> permisosPorModulo();
}
