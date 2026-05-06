package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.request.ProfesionalUpdateDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfesionalService {
    // --- ACTUALIZADO ---
    ProfesionalResponseDTO registrarProfesional(ProfesionalRegistroDTO dto, MultipartFile foto);
    
    ProfesionalResponseDTO obtenerProfesionalPorId(Long id);
    List<ProfesionalResponseDTO> obtenerTodosLosProfesionales();
    List<ProfesionalResponseDTO> obtenerProfesionalesConMembresiaInactiva();
    ProfesionalResponseDTO actualizarProfesional(Long id, ProfesionalUpdateDTO dto);
    void eliminarSoloProfesional(Long id);
    
    void confirmarCuenta(String token);

    // --- NUEVOS MÉTODOS DE MEMBRESÍA ---
    void verificarMatricula(Long idProfesional); 
    void otorgarAccesoIndefinido(Long idProfesional);
    // --- NUEVO MÉTODO ---
    void reenviarCorreoConfirmacion(String email);
}