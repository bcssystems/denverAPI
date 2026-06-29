package com.bcsystems.intranet.service;

import com.bcsystems.intranet.dto.AtributoRequest;
import com.bcsystems.intranet.dto.AtributoResponse;

import java.util.List;

public interface AtributoService {
    List<AtributoResponse> listar();
    AtributoResponse obtenerPorId(Integer id);
    AtributoResponse crear(AtributoRequest request);
    AtributoResponse actualizar(Integer id, AtributoRequest request);
    void eliminar(Integer id);
    List<AtributoResponse> listarActivos();
}
