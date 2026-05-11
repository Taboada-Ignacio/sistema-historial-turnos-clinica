package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.response.RolResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.mapper.RolMapper;
import com.clinica.usuarios.model.Rol;
import com.clinica.usuarios.repository.RolRepository;
import com.clinica.usuarios.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final RolMapper rolMapper;

    @Override
    @Transactional(readOnly = true)
    public RolResponseDTO obtenerRolPorId(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el rol con ID: " + id));

        return rolMapper.toResponseDTO(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolResponseDTO> obtenerTodosLosRoles() {
        return rolRepository.findAll().stream()
                .map(rolMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
