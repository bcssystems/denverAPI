package com.bcsystems.intranet.service;

import com.bcsystems.intranet.dto.ClienteRequest;
import com.bcsystems.intranet.dto.ClienteResponse;
import com.bcsystems.intranet.dto.ListaNegraRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ClienteService {
    Page<ClienteResponse> listar(String search, Boolean activo, int page, int size);
    Page<ClienteResponse> listarCreditClients(int page, int size);
    ClienteResponse obtenerPorId(Integer id);
    ClienteResponse crear(ClienteRequest request);
    ClienteResponse actualizar(Integer id, ClienteRequest request);
    void eliminar(Integer id);
    List<ClienteResponse> listarEnListaNegra();
    ClienteResponse cambiarListaNegra(Integer id, ListaNegraRequest request);
}