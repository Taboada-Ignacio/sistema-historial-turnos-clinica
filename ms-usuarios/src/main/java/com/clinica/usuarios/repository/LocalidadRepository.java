package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Localidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Long> {

    // Este lo dejamos tal cual, sirve para validar que no repitas nombres en una misma provincia
    boolean existsByNombreAndProvinciaIdProvincia(String nombre, Long idProvincia);

    /**
     * Usamos @Query para mantener el nombre 'findByProvinciaId'.
     * Esto soluciona:
     * 1. El error de compilación en el Service (porque el nombre coincide).
     * 2. El error de Docker (porque le decimos exactamente qué columna usar: idProvincia).
     */
    @Query("SELECT l FROM Localidad l WHERE l.provincia.idProvincia = :idProvincia")
    List<Localidad> findByProvinciaId(@Param("idProvincia") Long idProvincia);

    Optional<Localidad> findByNombreAndProvincia_IdProvincia(String nombre, Long idProvincia);

    @Query("""
            SELECT l FROM Localidad l
            JOIN FETCH l.provincia p
            WHERE (:provinciaId IS NULL OR p.idProvincia = :provinciaId)
              AND (:nombrePattern IS NULL OR UPPER(l.nombre) LIKE :nombrePattern)
            ORDER BY p.nombre ASC, l.nombre ASC
            """)
    List<Localidad> buscar(
            @Param("provinciaId") Long provinciaId,
            @Param("nombrePattern") String nombrePattern);
}