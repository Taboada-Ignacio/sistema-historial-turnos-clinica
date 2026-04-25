package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;

// FIJATE AQUÍ: Debe decir 'interface', no 'class'
public interface PacienteService {
    PacienteResponseDTO registrarPaciente(PacienteRegistroDTO dto);
}