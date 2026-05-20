package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.DireccionRegistroDTO;
import com.clinica.usuarios.dto.request.DireccionUpdateDTO;
import com.clinica.usuarios.dto.response.DireccionResponseDTO;
import com.clinica.usuarios.model.Direccion;
import com.clinica.usuarios.model.Localidad;

import java.util.List;

public interface DireccionService {

    List<DireccionResponseDTO> listarPorLocalidad(Long idLocalidad);

    DireccionResponseDTO obtenerPorId(Long id);

    DireccionResponseDTO registrar(DireccionRegistroDTO dto);

    DireccionResponseDTO actualizar(Long id, DireccionUpdateDTO dto);

    void eliminar(Long id);

    /**
     * Busca por texto normalizado en la localidad o crea una nueva fila.
     */
    Direccion obtenerOCrearPorTextoYLocalidad(String texto, Localidad localidad);
}
