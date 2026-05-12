package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.DireccionRegistroDTO;
import com.clinica.usuarios.dto.response.DireccionResponseDTO;
import com.clinica.usuarios.model.Direccion;
import com.clinica.usuarios.model.Localidad;

import java.util.List;

public interface DireccionService {

    List<DireccionResponseDTO> listarPorLocalidad(Long idLocalidad);

    DireccionResponseDTO registrar(DireccionRegistroDTO dto);

    /**
     * Busca por texto normalizado (trim, espacios colapsados, Unicode NFC) en la localidad
     * o crea una nueva fila. Reutiliza la existente ante duplicado o carrera concurrente.
     */
    Direccion obtenerOCrearPorTextoYLocalidad(String texto, Localidad localidad);
}