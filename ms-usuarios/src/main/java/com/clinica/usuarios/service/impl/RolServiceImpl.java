package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.RolRegistroDTO;
import com.clinica.usuarios.dto.request.RolUpdateDTO;
import com.clinica.usuarios.dto.response.RolResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.RolMapper;
import com.clinica.usuarios.model.Rol;
import com.clinica.usuarios.repository.RolRepository;
import com.clinica.usuarios.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    @Transactional
    public RolResponseDTO registrarRol(RolRegistroDTO dto) {
        
        // Convertimos a mayúsculas para estandarizar (opcional pero recomendado)
        String descripcionMayuscula = dto.getDescripcion().toUpperCase();

        if (rolRepository.findByDescripcion(descripcionMayuscula).isPresent()) {
            throw new ReglaDeNegocioException("El rol '" + descripcionMayuscula + "' ya existe en el sistema.");
        }

        Rol rol = rolMapper.toEntity(dto);
        rol.setDescripcion(descripcionMayuscula);
        
        Rol rolGuardado = rolRepository.save(rol);
        return rolMapper.toResponseDTO(rolGuardado);
    }

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

    @Override
    @Transactional
    public RolResponseDTO actualizarRol(Long id, RolUpdateDTO dto) {
        
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con ID: " + id));

        String nuevaDescripcion = dto.getDescripcion().toUpperCase();

        // Verificamos que el nuevo nombre no esté siendo usado por OTRO rol distinto
        rolRepository.findByDescripcion(nuevaDescripcion).ifPresent(rolExistente -> {
            if (!rolExistente.getIdRol().equals(id)) {
                throw new ReglaDeNegocioException("El rol '" + nuevaDescripcion + "' ya está en uso.");
            }
        });

        rol.setDescripcion(nuevaDescripcion);
        rol = rolRepository.save(rol);

        return rolMapper.toResponseDTO(rol);
    }

    @Override
    @Transactional
    public void eliminarRol(Long id) {
        if (!rolRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("El rol con ID " + id + " no fue encontrado.");
        }

        try {
            rolRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaDeNegocioException(
                "No se puede eliminar el rol con ID " + id + 
                " porque hay usuarios (pacientes/profesionales) que lo tienen asignado."
            );
        }
    }
}