package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.CorteCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CorteCajaRepository extends JpaRepository<CorteCaja, Integer> {
    Optional<CorteCaja> findTopByCajaIdCajaOrderByFechaCierreDesc(Integer idCaja);
}
