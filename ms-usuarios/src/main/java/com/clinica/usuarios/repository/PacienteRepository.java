package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
    // Ejemplo de búsqueda específica de un paciente por su número de afiliado
    Optional<Paciente> findByNumeroAfiliado(String numeroAfiliado);
}