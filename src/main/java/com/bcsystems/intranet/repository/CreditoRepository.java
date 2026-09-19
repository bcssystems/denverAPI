package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.Credito;
import com.bcsystems.intranet.domain.en.EstadoCredito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CreditoRepository extends JpaRepository<Credito, Integer> {

    boolean existsByFolio(String folio);

    List<Credito> findByClienteIdClienteAndEstadoOrderByFechaCreacionDesc(Integer idCliente, EstadoCredito estado);

    List<Credito> findByClienteIdClienteOrderByFechaCreacionDesc(Integer idCliente);

    @Query("SELECT c FROM Credito c WHERE c.cliente.idCliente = :idCliente ORDER BY c.fechaCreacion DESC")
    List<Credito> findActivosByCliente(@Param("idCliente") Integer idCliente);
}
