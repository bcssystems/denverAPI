package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    Optional<Persona> findByUsuarioIgnoreCase(String usuario);
    Optional<Persona> findByUsuario(String usuario);
    boolean existsByUsuarioIgnoreCase(String usuario);
    boolean existsByUsuarioIgnoreCaseAndIdPersonaNot(String usuario, Integer idPersona);

    List<Persona> findByRolIsNull();

    @Query(value = "SELECT rol FROM persona WHERE id_persona = :idPersona", nativeQuery = true)
    Optional<String> findLegacyRolColumn(@Param("idPersona") Integer idPersona);
}
