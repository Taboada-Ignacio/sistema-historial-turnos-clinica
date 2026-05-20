package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Spring Boot traduce esto a: SELECT * FROM usuarios WHERE email = ?
    Optional<Usuario> findByEmail(String email);

    // Spring Boot traduce esto a: SELECT * FROM usuarios WHERE dni = ?
    Optional<Usuario> findByDni(Integer dni);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Usuario u SET u.direccion.idDireccion = :sentinelId WHERE u.direccion.idDireccion = :oldId")
    int reasignarDireccion(@Param("oldId") Long oldId, @Param("sentinelId") Long sentinelId);
}