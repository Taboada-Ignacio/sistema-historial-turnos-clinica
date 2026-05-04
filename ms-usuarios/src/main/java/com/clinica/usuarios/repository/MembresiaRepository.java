package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {
    
    // Buscar una membresía por su nombre (ej: "SIN_VERIFICAR", "ACTIVA")
    Optional<Membresia> findByNombre(String nombre);
}