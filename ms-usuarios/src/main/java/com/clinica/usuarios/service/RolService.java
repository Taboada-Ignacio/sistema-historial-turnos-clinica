package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.RolRegistroDTO;
import com.clinica.usuarios.dto.request.RolUpdateDTO;
import com.clinica.usuarios.dto.response.RolResponseDTO;

import java.util.List;

public interface RolService {
    RolResponseDTO registrarRol(RolRegistroDTO dto);
    
    RolResponseDTO obtenerRolPorId(Long id);
    
    List<RolResponseDTO> obtenerTodosLosRoles();

    RolResponseDTO actualizarRol(Long id, RolUpdateDTO dto);

    void eliminarRol(Long id);
}