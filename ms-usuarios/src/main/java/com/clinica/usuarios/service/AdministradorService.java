package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.request.AdministradorUpdateDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;
import java.util.List;

public interface AdministradorService {

    // Agregamos el parámetro String providedSecret aquí:
    AdministradorResponseDTO registrarAdministrador(AdministradorRegistroDTO dto, String providedSecret);

    AdministradorResponseDTO obtenerAdministradorPorId(Long id);

    List<AdministradorResponseDTO> obtenerTodosLosAdministradores();

    AdministradorResponseDTO actualizarAdministrador(Long id, AdministradorUpdateDTO dto);

    void eliminarAdministrador(Long id);
}