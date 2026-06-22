package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.TipoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoPagoRepository extends JpaRepository<TipoPago, Integer> {
    List<TipoPago> findByActivoTrueOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdTipoPagoNot(String nombre, Integer idTipoPago);
}
