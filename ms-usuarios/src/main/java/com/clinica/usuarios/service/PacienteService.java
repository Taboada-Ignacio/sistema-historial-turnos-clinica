package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.request.PacienteUpdateDTO;
import com.clinica.usuarios.dto.response.PacienteBusquedaResponseDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;

import java.util.List;

public interface PacienteService {
    PacienteResponseDTO registrarPaciente(PacienteRegistroDTO dto);
    PacienteResponseDTO obtenerPacientePorId(Long id);
    List<PacienteResponseDTO> obtenerTodosLosPacientes();

    /**
     * Búsqueda admin por criterios independientes y combinables:
     * solo nombre, solo provincia, provincia+localidad, provincia+nombre, o los tres.
     * {@code idLocalidad} exige {@code idProvincia}.
     */
    PacienteBusquedaResponseDTO buscarPacientes(String texto, Long idProvincia, Long idLocalidad);
    PacienteResponseDTO actualizarPaciente(Long id, PacienteUpdateDTO dto);
    void eliminarSoloPaciente(Long id);
    
    // Double opt-in
    void confirmarCuenta(String token);

    void confirmarCuentaConCodigo(String email, String codigo);

    void reenviarCorreoConfirmacion(String email);
}