package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.ObraSocialRegistroDTO;
import com.clinica.usuarios.dto.request.ObraSocialUpdateDTO;
import com.clinica.usuarios.dto.response.ObraSocialResponseDTO;

import java.util.List;

public interface ObraSocialService {
    ObraSocialResponseDTO registrarObraSocial(ObraSocialRegistroDTO dto);
    
    ObraSocialResponseDTO obtenerObraSocialPorId(Long id);
    
    List<ObraSocialResponseDTO> obtenerTodasLasObrasSociales();

    ObraSocialResponseDTO actualizarObraSocial(Long id, ObraSocialUpdateDTO dto);

    void eliminarObraSocial(Long id);
}