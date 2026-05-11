package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {

    List<Direccion> findByLocalidad_IdLocalidadOrderByNombreAsc(Long idLocalidad);

    boolean existsByNombreAndLocalidad_IdLocalidad(String nombre, Long idLocalidad);

    Optional<Direccion> findByNombreAndLocalidad_IdLocalidad(String nombre, Long idLocalidad);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Direccion d SET d.localidad.idLocalidad = :sentinelId WHERE d.localidad.idLocalidad = :oldId")
    int reasignarLocalidad(@Param("oldId") Long oldId, @Param("sentinelId") Long sentinelId);
}
