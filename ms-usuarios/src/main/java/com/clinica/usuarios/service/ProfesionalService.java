package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.request.ProfesionalUpdateDTO;
import com.clinica.usuarios.dto.response.ProfesionalPresentacionDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfesionalService {
    // --- ACTUALIZADO ---
    ProfesionalResponseDTO registrarProfesional(ProfesionalRegistroDTO dto, MultipartFile foto);
    
    ProfesionalResponseDTO obtenerProfesionalPorId(Long id);

    /** Perfil del profesional autenticado (email del JWT). */
    ProfesionalResponseDTO obtenerProfesionalSesion(String email);
    List<ProfesionalResponseDTO> obtenerTodosLosProfesionales();

    /**
     * Listado admin filtrado por nombre de membresía actual (debe existir en catálogo, p. ej. SIN_VERIFICAR, INACTIVA, ACTIVA).
     */
    List<ProfesionalResponseDTO> obtenerProfesionalesPorMembresiaNombre(String nombreMembresia);
    List<ProfesionalPresentacionDTO> listarParaPresentacion();
    /**
     * Ficha de presentación. Si {@code emailSolicitante} coincide con el profesional, se devuelve aunque no esté ACTIVO (p. ej. panel propio).
     */
    ProfesionalPresentacionDTO obtenerParaPresentacion(Long id, String emailSolicitante);
    List<ProfesionalResponseDTO> obtenerProfesionalesConMembresiaInactiva();
    ProfesionalResponseDTO actualizarProfesional(Long id, ProfesionalUpdateDTO dto, MultipartFile foto);
    void eliminarSoloProfesional(Long id);
    
    void confirmarCuenta(String token);

    void confirmarCuentaConCodigo(String email, String codigo);

    // --- NUEVOS MÉTODOS DE MEMBRESÍA ---
    void verificarMatricula(Long idProfesional); 
    void otorgarAccesoIndefinido(Long idProfesional);
    // --- NUEVO MÉTODO ---
    void reenviarCorreoConfirmacion(String email);
}