package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Localidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Long> {
    
    // Spring traduce esto a: SELECT * FROM localidades WHERE id_provincia = ?
    List<Localidad> findByProvinciaIdProvincia(Long idProvincia);
}