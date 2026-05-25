package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.CambioMembresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CambioMembresiaRepository extends JpaRepository<CambioMembresia, Long> {
    
    // Obtener todo el historial de membresías de un profesional, ordenado desde el más reciente al más antiguo
    List<CambioMembresia> findByProfesional_IdProfesionalOrderByFechaInicioDesc(Long idProfesional);

    void deleteByProfesional_IdProfesional(Long idProfesional);
}