package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Profesional;
import com.clinica.usuarios.model.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {
    
    // Buscar un profesional por su matrícula
    Optional<Profesional> findByMatricula(String matricula);

    // Buscar todos los profesionales que tengan una membresía específica (ej: listar los "SIN_VERIFICAR" para aprobarlos)
    List<Profesional> findByMembresiaActual_Nombre(String nombreMembresia);

    // Buscar profesionales por objeto Membresia
    List<Profesional> findByMembresiaActual(Membresia membresia);

    @Query("""
            SELECT DISTINCT p FROM Profesional p
            LEFT JOIN FETCH p.localidad loc
            LEFT JOIN FETCH loc.provincia
            LEFT JOIN FETCH p.direccion
            JOIN FETCH p.especialidad
            JOIN FETCH p.estadoActual
            """)
    List<Profesional> findAllWithUbicacionAndEspecialidad();

    @Query("""
            SELECT p FROM Profesional p
            LEFT JOIN FETCH p.localidad loc
            LEFT JOIN FETCH loc.provincia
            LEFT JOIN FETCH p.direccion
            JOIN FETCH p.especialidad
            JOIN FETCH p.estadoActual
            WHERE p.idUsuario = :id
            """)
    Optional<Profesional> findWithUbicacionById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Profesional p SET p.especialidad.idEspecialidad = :sentinelId WHERE p.especialidad.idEspecialidad = :oldId")
    int reasignarEspecialidad(@Param("oldId") Long oldId, @Param("sentinelId") Long sentinelId);
}