package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.request.AdministradorUpdateDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;

import java.util.List;

public interface AdministradorService {

    AdministradorResponseDTO registrarAdministrador(AdministradorRegistroDTO dto);
    
    AdministradorResponseDTO obtenerAdministradorPorId(Long id);
    
    List<AdministradorResponseDTO> obtenerTodosLosAdministradores();
    
    AdministradorResponseDTO actualizarAdministrador(Long id, AdministradorUpdateDTO dto);
    
    void eliminarAdministrador(Long id);
}