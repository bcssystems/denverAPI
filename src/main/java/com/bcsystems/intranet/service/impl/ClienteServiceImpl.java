package com.bcsystems.intranet.service.impl;

import com.bcsystems.intranet.domain.Cliente;
import com.bcsystems.intranet.dto.ClienteRequest;
import com.bcsystems.intranet.dto.ClienteResponse;
import com.bcsystems.intranet.dto.ListaNegraRequest;
import com.bcsystems.intranet.exception.InvalidEntryException;
import com.bcsystems.intranet.exception.NotFoundException;
import com.bcsystems.intranet.repository.ClienteRepository;
import com.bcsystems.intranet.service.AuditoriaService;
import com.bcsystems.intranet.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;

    @Override
    public Page<ClienteResponse> listar(String search, Boolean activo, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("nombre"));
        Page<Cliente> clientes;
        if (activo == null) {
            if (search != null && !search.isBlank()) {
                clientes = clienteRepository.buscar(search, pageable);
            } else {
                clientes = clienteRepository.findActivos(pageable);
            }
        } else {
            clientes = clienteRepository.buscarConActivo(search, activo, pageable);
        }
        return clientes.map(this::toResponse);
    }

    @Override
    public Page<ClienteResponse> listarCreditClients(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("nombre"));
        return clienteRepository.findCreditClients(pageable).map(this::toResponse);
    }

    @Override
    public ClienteResponse obtenerPorId(Integer id) {
        return toResponse(buscarOExcepcion(id));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        Cliente cliente = Cliente.builder()
                .nombre(request.nombre())
                .apellidoPaterno(request.apellidoPaterno())
                .apellidoMaterno(request.apellidoMaterno())
                .telefono(request.telefono())
                .codigoPais(request.codigoPais() != null ? request.codigoPais() : "+52")
                .whatsapp(request.whatsapp())
                .empresa(request.empresa())
                .regimenFiscal(request.regimenFiscal())
                .cp(request.cp())
                .direccion(request.direccion())
                .calle(request.calle())
                .numExt(request.numExt())
                .numInt(request.numInt())
                .colonia(request.colonia())
                .municipio(request.municipio())
                .estado(request.estado())
                .rfc(request.rfc())
                .representanteLegal(request.representanteLegal())
                .direccionEntrega(request.direccionEntrega())
                .tieneCredito(request.tieneCredito() != null && request.tieneCredito())
                .limiteCredito(request.limiteCredito())
                .saldoActual(0.0)
                .enListaNegra(false)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();
        cliente = clienteRepository.save(cliente);

        auditoriaService.registrar("Cliente", cliente.getIdCliente(), "CREACION",
                obtenerUsuarioActual(), "Cliente creado: " + cliente.getNombre());
        return toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {
        Cliente cliente = buscarOExcepcion(id);
        cliente.setNombre(request.nombre());
        cliente.setApellidoPaterno(request.apellidoPaterno());
        cliente.setApellidoMaterno(request.apellidoMaterno());
        cliente.setTelefono(request.telefono());
        cliente.setCodigoPais(request.codigoPais() != null ? request.codigoPais() : "+52");
        cliente.setWhatsapp(request.whatsapp());
        cliente.setEmpresa(request.empresa());
        cliente.setRegimenFiscal(request.regimenFiscal());
        cliente.setCp(request.cp());
        cliente.setDireccion(request.direccion());
        cliente.setCalle(request.calle());
        cliente.setNumExt(request.numExt());
        cliente.setNumInt(request.numInt());
        cliente.setColonia(request.colonia());
        cliente.setMunicipio(request.municipio());
        cliente.setEstado(request.estado());
        cliente.setRfc(request.rfc());
        cliente.setRepresentanteLegal(request.representanteLegal());
        cliente.setDireccionEntrega(request.direccionEntrega());
        cliente.setTieneCredito(request.tieneCredito() != null && request.tieneCredito());
        cliente.setLimiteCredito(request.limiteCredito());
        cliente = clienteRepository.save(cliente);

        auditoriaService.registrar("Cliente", id, "ACTUALIZACION",
                obtenerUsuarioActual(), "Cliente actualizado: " + cliente.getNombre());
        return toResponse(cliente);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Cliente cliente = buscarOExcepcion(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Override
    public List<ClienteResponse> listarEnListaNegra() {
        return clienteRepository.findByEnListaNegraTrueOrderByFechaListaNegraDesc().stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ClienteResponse cambiarListaNegra(Integer id, ListaNegraRequest request) {
        Cliente cliente = buscarOExcepcion(id);
        if (Boolean.TRUE.equals(request.enListaNegra()) && Boolean.TRUE.equals(cliente.getEnListaNegra())) {
            throw new InvalidEntryException("El cliente ya está en lista negra");
        }
        if (!Boolean.TRUE.equals(request.enListaNegra()) && !Boolean.TRUE.equals(cliente.getEnListaNegra())) {
            throw new InvalidEntryException("El cliente no está en lista negra");
        }
        cliente.setEnListaNegra(request.enListaNegra());
        if (Boolean.TRUE.equals(request.enListaNegra())) {
            cliente.setFechaListaNegra(LocalDateTime.now());
            cliente.setMotivoListaNegra(request.motivo());
        } else {
            cliente.setFechaListaNegra(null);
            cliente.setMotivoListaNegra(null);
        }
        cliente = clienteRepository.save(cliente);

        auditoriaService.registrar("Cliente", id, "ACTUALIZACION",
                obtenerUsuarioActual(),
                Boolean.TRUE.equals(request.enListaNegra())
                        ? "Cliente agregado a lista negra: " + request.motivo()
                        : "Cliente removido de lista negra");
        return toResponse(cliente);
    }

    private Cliente buscarOExcepcion(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado con id: " + id));
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(
                c.getIdCliente(), c.getNombre(),
                c.getApellidoPaterno(), c.getApellidoMaterno(),
                c.getTelefono(), c.getCodigoPais(),
                c.getWhatsapp(), c.getEmpresa(),
                c.getRegimenFiscal(), c.getCp(), c.getDireccion(),
                c.getCalle(), c.getNumExt(), c.getNumInt(), c.getColonia(),
                c.getMunicipio(), c.getEstado(),
                c.getRfc(), c.getRepresentanteLegal(), c.getDireccionEntrega(),
                c.getActivo(), c.getFechaRegistro(),
                c.getTieneCredito(), c.getLimiteCredito(), c.getSaldoActual(),
                c.getEnListaNegra(), c.getFechaListaNegra(), c.getMotivoListaNegra());
    }
}