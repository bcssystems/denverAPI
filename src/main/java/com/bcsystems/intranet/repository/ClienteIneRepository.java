package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.ClienteIne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteIneRepository extends JpaRepository<ClienteIne, Integer> {
    Optional<ClienteIne> findByClienteIdCliente(Integer idCliente);
}