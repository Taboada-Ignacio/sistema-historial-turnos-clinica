package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Spring Boot traduce esto a: SELECT * FROM usuarios WHERE email = ?
    Optional<Usuario> findByEmail(String email);

    // Spring Boot traduce esto a: SELECT * FROM usuarios WHERE dni = ?
    Optional<Usuario> findByDni(Integer dni);
}