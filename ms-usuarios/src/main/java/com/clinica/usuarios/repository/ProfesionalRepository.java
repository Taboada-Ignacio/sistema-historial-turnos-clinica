package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {
    
    // Buscar un profesional por su matrícula
    Optional<Profesional> findByMatricula(String matricula);

    // Buscar todos los profesionales que tengan una membresía específica (ej: listar los "SIN_VERIFICAR" para aprobarlos)
    List<Profesional> findByMembresiaActual_Nombre(String nombreMembresia);
}