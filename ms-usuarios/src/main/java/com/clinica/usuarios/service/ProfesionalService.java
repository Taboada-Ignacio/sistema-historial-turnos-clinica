package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.request.ProfesionalUpdateDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;

import java.util.List;

public interface ProfesionalService {
    ProfesionalResponseDTO registrarProfesional(ProfesionalRegistroDTO dto);
    
    ProfesionalResponseDTO obtenerProfesionalPorId(Long id);
    
    List<ProfesionalResponseDTO> obtenerTodosLosProfesionales();

    ProfesionalResponseDTO actualizarProfesional(Long id, ProfesionalUpdateDTO dto);

    void eliminarSoloProfesional(Long id);
}