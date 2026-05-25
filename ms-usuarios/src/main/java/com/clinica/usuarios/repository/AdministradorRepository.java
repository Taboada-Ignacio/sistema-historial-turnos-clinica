package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long>, JpaSpecificationExecutor<Administrador> {

    Optional<Administrador> findByDni(Integer dni);

    Optional<Administrador> findByUsuario_Email(String email);

    Optional<Administrador> findByUsuario_IdUsuario(Long idUsuario);

    @Query("""
            SELECT a FROM Administrador a
            JOIN FETCH a.usuario u
            JOIN FETCH u.estadoActual
            LEFT JOIN FETCH a.direccion dir
            LEFT JOIN FETCH dir.localidad loc
            LEFT JOIN FETCH loc.provincia
            WHERE u.idUsuario = :idUsuario
            """)
    Optional<Administrador> findWithUbicacionByUsuarioId(@Param("idUsuario") Long idUsuario);

    default Optional<Administrador> findWithUbicacionById(Long idUsuario) {
        return findWithUbicacionByUsuarioId(idUsuario);
    }

    @Query("""
            SELECT a FROM Administrador a
            JOIN FETCH a.usuario u
            JOIN FETCH u.estadoActual
            ORDER BY a.apellido ASC, a.nombre ASC
            """)
    List<Administrador> findAllWithUsuario();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Administrador a SET a.direccion.idDireccion = :sentinelId WHERE a.direccion.idDireccion = :oldId")
    int reasignarDireccion(@Param("oldId") Long oldId, @Param("sentinelId") Long sentinelId);
}
