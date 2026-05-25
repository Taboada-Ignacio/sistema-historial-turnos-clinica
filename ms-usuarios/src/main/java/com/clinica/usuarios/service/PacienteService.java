package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.PacienteCargaProfesionalDTO;
import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.request.PacienteUpdateDTO;
import com.clinica.usuarios.dto.response.PacienteActivacionDatosDTO;
import com.clinica.usuarios.dto.response.PacienteBusquedaResponseDTO;
import com.clinica.usuarios.dto.response.PacienteCargaProfesionalResponseDTO;
import com.clinica.usuarios.dto.response.PacientePortalSesionDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;

import java.util.List;

public interface PacienteService {
    PacienteResponseDTO registrarPaciente(PacienteRegistroDTO dto);
    PacienteResponseDTO obtenerPacientePorId(Long id);
    PacientePortalSesionDTO obtenerPacienteSesion(String email);
    List<PacienteResponseDTO> obtenerTodosLosPacientes();

    /**
     * Búsqueda admin por criterios independientes y combinables:
     * solo nombre, solo provincia, provincia+localidad, provincia+nombre, o los tres.
     * {@code idLocalidad} exige {@code idProvincia}.
     */
    PacienteBusquedaResponseDTO buscarPacientes(String texto, Long idProvincia, Long idLocalidad);

    /**
     * Búsqueda acotada a una provincia y localidad (portal profesional).
     * {@code texto} obligatorio: apellido, nombre o DNI.
     */
    PacienteBusquedaResponseDTO buscarPacientesEnUbicacion(String texto, Long idProvincia, Long idLocalidad);
    PacienteResponseDTO actualizarPaciente(Long id, PacienteUpdateDTO dto);
    void eliminarSoloPaciente(Long id);
    
    // Double opt-in
    void confirmarCuenta(String token);

    void confirmarCuentaConCodigo(String email, String codigo);

    void reenviarCorreoConfirmacion(String email);

    /** Alta por profesional: estado SIN_CONTRASENA y correo de activación (72 h). */
    PacienteCargaProfesionalResponseDTO cargarPacienteSinPassword(PacienteCargaProfesionalDTO dto);

    /**
     * Reenvío según estado: PENDIENTE → confirmación+código; SIN_CONTRASENA → activación;
     * ACTIVO → error; BLOQUEADO → error explícito.
     */
    void reenviarCorreoAccesoPaciente(String email);

    /** Recuperar contraseña: ramas por estado (PENDIENTE/SIN_CONTRASENA/ACTIVO/BLOQUEADO). */
    void solicitarCambioPasswordPaciente(String email);

    PacienteActivacionDatosDTO obtenerDatosActivacionPaciente(String token);

    void establecerPasswordInicialPaciente(String token, String passwordNueva);
}