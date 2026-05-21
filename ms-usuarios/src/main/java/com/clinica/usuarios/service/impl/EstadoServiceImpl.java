package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.response.EstadoDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.mapper.EstadoMapper;
import com.clinica.usuarios.model.Estado;
import com.clinica.usuarios.repository.EstadoRepository;
import com.clinica.usuarios.service.EstadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadoServiceImpl implements EstadoService {

    private final EstadoRepository estadoRepository;
    private final EstadoMapper estadoMapper;

    @Override
    @Transactional(readOnly = true)
    public EstadoDTO obtenerPorId(Long id) {
        Estado estado = estadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el estado con ID: " + id));
        return estadoMapper.toDTO(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoDTO> obtenerTodos() {
        return estadoRepository.findAll().stream()
                .sorted(Comparator.comparing(Estado::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(estadoMapper::toDTO)
                .collect(Collectors.toList());
    }
}
