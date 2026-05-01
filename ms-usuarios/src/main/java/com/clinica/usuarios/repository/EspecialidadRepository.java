package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
    // Spring Boot lee el nombre del método y arma el "SELECT * FROM especialidad WHERE descripcion = ?"
    Optional<Especialidad> findByDescripcion(String descripcion);
}