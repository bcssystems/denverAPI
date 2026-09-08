package com.bcsystems.intranet.repository;

import com.bcsystems.intranet.domain.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Integer> {
    Optional<Permiso> findByClave(String clave);
    List<Permiso> findByModuloOrderByModuloAscClaveAsc(String modulo);
    List<Permiso> findAllByOrderByModuloAscClaveAsc();
    boolean existsByClave(String clave);
}
