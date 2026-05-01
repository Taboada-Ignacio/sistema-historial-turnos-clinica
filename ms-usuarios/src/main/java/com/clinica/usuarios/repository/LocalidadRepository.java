package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Localidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Long> {
    // Usamos el ID de la provincia para la búsqueda compuesta
    boolean existsByNombreAndProvinciaIdProvincia(String nombre, Long idProvincia);
}