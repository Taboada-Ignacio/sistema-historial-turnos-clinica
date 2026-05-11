package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.response.RolResponseDTO;

import java.util.List;

public interface RolService {

    RolResponseDTO obtenerRolPorId(Long id);

    List<RolResponseDTO> obtenerTodosLosRoles();
}
