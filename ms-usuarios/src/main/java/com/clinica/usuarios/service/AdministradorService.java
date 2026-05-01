package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;

public interface AdministradorService {

    /**
     * Registra un nuevo administrador en el sistema.
     * 
     * @param dto Datos de registro del administrador.
     * @return DTO con los datos del administrador guardado.
     */
    AdministradorResponseDTO registrarAdministrador(AdministradorRegistroDTO dto);
    
}