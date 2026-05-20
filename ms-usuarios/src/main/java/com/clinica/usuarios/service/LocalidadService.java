package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.LocalidadRegistroDTO;
import com.clinica.usuarios.dto.request.LocalidadUpdateDTO;
import com.clinica.usuarios.dto.response.LocalidadResponseDTO;

import java.util.List;

public interface LocalidadService {
    
    LocalidadResponseDTO registrarLocalidad(LocalidadRegistroDTO dto);
    
    LocalidadResponseDTO obtenerLocalidadPorId(Long id);
    
    List<LocalidadResponseDTO> obtenerTodasLasLocalidades();

    /**
     * Listado con filtros opcionales reutilizable (admin, registro, combos).
     *
     * @param provinciaId si no es null, solo localidades de esa provincia (404 si no existe)
     * @param nombre      búsqueda parcial por nombre (trim; vacío = sin filtro)
     */
    List<LocalidadResponseDTO> buscarLocalidades(Long provinciaId, String nombre);

    LocalidadResponseDTO actualizarLocalidad(Long id, LocalidadUpdateDTO dto);

    void eliminarLocalidad(Long id);

    List<LocalidadResponseDTO> obtenerLocalidadesPorProvincia(Long provinciaId);
}