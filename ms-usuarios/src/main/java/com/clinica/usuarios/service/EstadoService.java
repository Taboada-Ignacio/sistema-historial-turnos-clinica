package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.response.EstadoDTO;

import java.util.List;

public interface EstadoService {

    EstadoDTO obtenerPorId(Long id);

    List<EstadoDTO> obtenerTodos();
}
