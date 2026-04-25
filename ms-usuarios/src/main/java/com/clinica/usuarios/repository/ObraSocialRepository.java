package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.ObraSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObraSocialRepository extends JpaRepository<ObraSocial, Long> {
    
    // Útil para validar que no existan duplicados al cargar el catálogo
    Optional<ObraSocial> findByDescripcion(String descripcion);
}