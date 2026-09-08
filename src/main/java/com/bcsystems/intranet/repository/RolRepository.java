package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdRolNot(String nombre, Integer idRol);
    List<Rol> findByActivoTrueOrderByNombre();
}
