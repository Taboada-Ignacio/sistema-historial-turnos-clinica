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
     * Busca por texto exacto (tras trim) en la localidad o crea una nueva fila.
     * Así el catálogo solo crece con direcciones realmente usadas en registros.
     */
    Direccion obtenerOCrearPorTextoYLocalidad(String texto, Localidad localidad);
}