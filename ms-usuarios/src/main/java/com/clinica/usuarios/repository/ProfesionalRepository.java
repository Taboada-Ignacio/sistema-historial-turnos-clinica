package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Membresia;
import com.clinica.usuarios.model.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Long>, JpaSpecificationExecutor<Profesional> {

    Optional<Profesional> findByMatricula(String matricula);

    Optional<Profesional> findByDni(Integer dni);

    Optional<Profesional> findByUsuario_Email(String email);

    Optional<Profesional> findByUsuario_IdUsuario(Long idUsuario);

    List<Profesional> findByMembresiaActual_Nombre(String nombreMembresia);

    List<Profesional> findByMembresiaActual(Membresia membresia);

    @Query("""
            SELECT DISTINCT p FROM Profesional p
            JOIN FETCH p.usuario u
            JOIN FETCH u.estadoActual
            LEFT JOIN FETCH p.direccion dir
            LEFT JOIN FETCH dir.localidad loc
            LEFT JOIN FETCH loc.provincia
            JOIN FETCH p.especialidad
            """)
    List<Profesional> findAllWithUbicacionAndEspecialidad();

    @Query("""
            SELECT p FROM Profesional p
            JOIN FETCH p.usuario u
            JOIN FETCH u.estadoActual
            LEFT JOIN FETCH p.direccion dir
            LEFT JOIN FETCH dir.localidad loc
            LEFT JOIN FETCH loc.provincia
            JOIN FETCH p.especialidad
            WHERE u.idUsuario = :idUsuario
            """)
    Optional<Profesional> findWithUbicacionByUsuarioId(@Param("idUsuario") Long idUsuario);

    default Optional<Profesional> findWithUbicacionById(Long idUsuario) {
        return findWithUbicacionByUsuarioId(idUsuario);
    }

    @Query("""
            SELECT p FROM Profesional p
            JOIN FETCH p.usuario u
            WHERE p.fotoPerfil LIKE CONCAT('%', :fileName)
            """)
    Optional<Profesional> findByFotoPerfilFileName(@Param("fileName") String fileName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Profesional p SET p.especialidad.idEspecialidad = :sentinelId WHERE p.especialidad.idEspecialidad = :oldId")
    int reasignarEspecialidad(@Param("oldId") Long oldId, @Param("sentinelId") Long sentinelId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Profesional p SET p.direccion.idDireccion = :sentinelId WHERE p.direccion.idDireccion = :oldId")
    int reasignarDireccion(@Param("oldId") Long oldId, @Param("sentinelId") Long sentinelId);
}
