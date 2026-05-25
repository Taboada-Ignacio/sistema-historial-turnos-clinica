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

    Optional<Paciente> findByNumeroAfiliado(String numeroAfiliado);

    Optional<Paciente> findByDni(Integer dni);

    Optional<Paciente> findByUsuario_Email(String email);

    @Query("""
            SELECT p FROM Paciente p
            JOIN FETCH p.usuario u
            JOIN FETCH u.estadoActual
            LEFT JOIN FETCH p.direccion dir
            LEFT JOIN FETCH dir.localidad loc
            LEFT JOIN FETCH loc.provincia
            JOIN FETCH p.obraSocial
            WHERE u.idUsuario = :idUsuario
            """)
    Optional<Paciente> findWithUbicacionByUsuarioId(@Param("idUsuario") Long idUsuario);

    default Optional<Paciente> findWithUbicacionById(Long idUsuario) {
        return findWithUbicacionByUsuarioId(idUsuario);
    }

    Optional<Paciente> findByUsuario_IdUsuario(Long idUsuario);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Paciente p SET p.obraSocial.idObraSocial = :sentinelId WHERE p.obraSocial.idObraSocial = :oldId")
    int reasignarObraSocial(@Param("oldId") Long oldId, @Param("sentinelId") Long sentinelId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Paciente p SET p.direccion.idDireccion = :sentinelId WHERE p.direccion.idDireccion = :oldId")
    int reasignarDireccion(@Param("oldId") Long oldId, @Param("sentinelId") Long sentinelId);
}
