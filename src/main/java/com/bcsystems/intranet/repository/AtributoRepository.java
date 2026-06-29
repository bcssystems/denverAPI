package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.Atributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtributoRepository extends JpaRepository<Atributo, Integer> {
    List<Atributo> findByActivoTrueOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdAtributoNot(String nombre, Integer idAtributo);
}
