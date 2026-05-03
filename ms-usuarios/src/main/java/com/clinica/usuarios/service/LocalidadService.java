package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.LocalidadRegistroDTO;
import com.clinica.usuarios.dto.request.LocalidadUpdateDTO;
import com.clinica.usuarios.dto.response.LocalidadResponseDTO;

import java.util.List;

public interface LocalidadService {
    
    LocalidadResponseDTO registrarLocalidad(LocalidadRegistroDTO dto);
    
    LocalidadResponseDTO obtenerLocalidadPorId(Long id);
    
    List<LocalidadResponseDTO> obtenerTodasLasLocalidades();

    LocalidadResponseDTO actualizarLocalidad(Long id, LocalidadUpdateDTO dto);

    void eliminarLocalidad(Long id);

    List<LocalidadResponseDTO> obtenerLocalidadesPorProvincia(Long provinciaId);
}