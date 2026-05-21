package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    @Query("""
            SELECT a FROM Administrador a
            LEFT JOIN FETCH a.direccion dir
            LEFT JOIN FETCH dir.localidad loc
            LEFT JOIN FETCH loc.provincia
            JOIN FETCH a.estadoActual
            WHERE a.idUsuario = :id
            """)
    Optional<Administrador> findWithUbicacionById(@Param("id") Long id);

    Optional<Administrador> findByEmail(String email);
}