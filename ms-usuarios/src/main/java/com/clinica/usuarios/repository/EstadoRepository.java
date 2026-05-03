package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {
    // Para buscar el estado por su nombre (ej: "PENDIENTE" o "ACTIVO")
    Optional<Estado> findByNombre(String nombre);
}