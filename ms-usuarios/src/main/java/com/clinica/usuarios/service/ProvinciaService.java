package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.ProvinciaRegistroDTO;
import com.clinica.usuarios.dto.request.ProvinciaUpdateDTO;
import com.clinica.usuarios.dto.response.ProvinciaResponseDTO;

import java.util.List;

public interface ProvinciaService {
    ProvinciaResponseDTO registrarProvincia(ProvinciaRegistroDTO dto);
    
    ProvinciaResponseDTO obtenerProvinciaPorId(Long id);
    
    List<ProvinciaResponseDTO> obtenerTodasLasProvincias();

    ProvinciaResponseDTO actualizarProvincia(Long id, ProvinciaUpdateDTO dto);

    void eliminarProvincia(Long id);
}