package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {
    
    // Buscar un profesional por su matrícula
    Optional<Profesional> findByNroMatricula(Integer nroMatricula);
}