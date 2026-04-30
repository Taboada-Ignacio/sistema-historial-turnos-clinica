package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.request.PacienteUpdateDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;

import java.util.List;

public interface PacienteService {
    PacienteResponseDTO registrarPaciente(PacienteRegistroDTO dto);
    
    PacienteResponseDTO obtenerPacientePorId(Long id);
    
    List<PacienteResponseDTO> obtenerTodosLosPacientes();

    PacienteResponseDTO actualizarPaciente(Long id, PacienteUpdateDTO dto);
}