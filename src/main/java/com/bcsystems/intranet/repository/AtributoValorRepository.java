package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.AtributoValor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtributoValorRepository extends JpaRepository<AtributoValor, Integer> {
    List<AtributoValor> findByAtributoIdAtributoAndActivoTrue(Integer idAtributo);
    List<AtributoValor> findByIdValorIn(List<Integer> ids);
}
