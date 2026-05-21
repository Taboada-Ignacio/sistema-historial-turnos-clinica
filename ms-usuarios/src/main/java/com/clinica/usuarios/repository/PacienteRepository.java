package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long>, JpaSpecificationExecutor<Paciente> {
    
    // Ejemplo de búsqueda específica de un paciente por su número de afiliado
    Optional<Paciente> findByNumeroAfiliado(String numeroAfiliado);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Paciente p SET p.obraSocial.idObraSocial = :sentinelId WHERE p.obraSocial.idObraSocial = :oldId")
    int reasignarObraSocial(@Param("oldId") Long oldId, @Param("sentinelId") Long sentinelId);

    @Query("""
            SELECT p FROM Paciente p
            LEFT JOIN FETCH p.direccion dir
            LEFT JOIN FETCH dir.localidad loc
            LEFT JOIN FETCH loc.provincia
            JOIN FETCH p.obraSocial
            JOIN FETCH p.estadoActual
            WHERE p.idUsuario = :id
            """)
    Optional<Paciente> findWithUbicacionById(@Param("id") Long id);
}