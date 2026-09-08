package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.PermisoAdicional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermisoAdicionalRepository extends JpaRepository<PermisoAdicional, Integer> {
    List<PermisoAdicional> findByPersonaIdPersona(Integer idPersona);

    @Modifying
    @Query("DELETE FROM PermisoAdicional pa WHERE pa.persona.idPersona = ?1")
    void deleteByPersonaIdPersona(Integer idPersona);
}
