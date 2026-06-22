package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.Abono;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbonoRepository extends JpaRepository<Abono, Integer> {

    List<Abono> findByCreditoIdCreditoOrderByFechaDesc(Integer idCredito);
}
