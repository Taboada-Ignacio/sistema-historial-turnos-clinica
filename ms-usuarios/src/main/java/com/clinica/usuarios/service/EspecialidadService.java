package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.EspecialidadRegistroDTO;
import com.clinica.usuarios.dto.request.EspecialidadUpdateDTO;
import com.clinica.usuarios.dto.response.EspecialidadResponseDTO;

import java.util.List;

public interface EspecialidadService {
    EspecialidadResponseDTO registrarEspecialidad(EspecialidadRegistroDTO dto);
    
    EspecialidadResponseDTO obtenerEspecialidadPorId(Long id);
    
    List<EspecialidadResponseDTO> obtenerTodasLasEspecialidades();

    EspecialidadResponseDTO actualizarEspecialidad(Long id, EspecialidadUpdateDTO dto);

    void eliminarEspecialidad(Long id);
}