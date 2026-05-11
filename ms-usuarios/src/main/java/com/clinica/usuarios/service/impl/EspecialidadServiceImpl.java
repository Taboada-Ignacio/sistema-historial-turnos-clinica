package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.EspecialidadRegistroDTO;
import com.clinica.usuarios.dto.request.EspecialidadUpdateDTO;
import com.clinica.usuarios.dto.response.EspecialidadResponseDTO;
import com.clinica.usuarios.constants.CatalogoSentinelConstants;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.EspecialidadMapper;
import com.clinica.usuarios.model.Especialidad;
import com.clinica.usuarios.repository.EspecialidadRepository;
import com.clinica.usuarios.repository.ProfesionalRepository;
import com.clinica.usuarios.service.EspecialidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;
    private final ProfesionalRepository profesionalRepository;
    private final EspecialidadMapper especialidadMapper;

    @Override
    @Transactional
    public EspecialidadResponseDTO registrarEspecialidad(EspecialidadRegistroDTO dto) {
        
        String descripcionMayuscula = dto.getDescripcion().toUpperCase();

        if (especialidadRepository.findByDescripcion(descripcionMayuscula).isPresent()) {
            throw new ReglaDeNegocioException("La especialidad '" + descripcionMayuscula + "' ya existe.");
        }

        Especialidad especialidad = especialidadMapper.toEntity(dto);
        especialidad.setDescripcion(descripcionMayuscula);
        
        Especialidad especialidadGuardada = especialidadRepository.save(especialidad);
        return especialidadMapper.toResponseDTO(especialidadGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public EspecialidadResponseDTO obtenerEspecialidadPorId(Long id) {
        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la especialidad con ID: " + id));
        
        return especialidadMapper.toResponseDTO(especialidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadResponseDTO> obtenerTodasLasEspecialidades() {
        return especialidadRepository.findAll().stream()
                .map(especialidadMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EspecialidadResponseDTO actualizarEspecialidad(Long id, EspecialidadUpdateDTO dto) {
        
        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada con ID: " + id));

        if (CatalogoSentinelConstants.esSentinelNombreODescripcion(especialidad.getDescripcion())) {
            throw new ReglaDeNegocioException("No se puede modificar el registro reservado del sistema.");
        }

        String nuevaDescripcion = dto.getDescripcion().toUpperCase();

        especialidadRepository.findByDescripcion(nuevaDescripcion).ifPresent(existente -> {
            // Cambiá .getIdEspecialidad() por .getId() si en tu entidad se llama así
            if (!existente.getIdEspecialidad().equals(id)) {
                throw new ReglaDeNegocioException("La especialidad '" + nuevaDescripcion + "' ya está en uso por otro ID.");
            }
        });

        especialidad.setDescripcion(nuevaDescripcion);
        especialidad = especialidadRepository.save(especialidad);

        return especialidadMapper.toResponseDTO(especialidad);
    }

    @Override
    @Transactional
    public void eliminarEspecialidad(Long id) {
        Especialidad esp = especialidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La especialidad con ID " + id + " no fue encontrada."));
        if (CatalogoSentinelConstants.esSentinelNombreODescripcion(esp.getDescripcion())) {
            throw new ReglaDeNegocioException("No se puede eliminar el registro reservado del sistema.");
        }
        Especialidad sentinel = especialidadRepository.findByDescripcion(CatalogoSentinelConstants.SIN_ESPECIFICAR)
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "Falta la especialidad reservada '" + CatalogoSentinelConstants.SIN_ESPECIFICAR + "' en la base de datos."));
        if (sentinel.getIdEspecialidad().equals(id)) {
            throw new ReglaDeNegocioException("No se puede eliminar el registro reservado del sistema.");
        }
        profesionalRepository.reasignarEspecialidad(id, sentinel.getIdEspecialidad());
        especialidadRepository.deleteById(id);
    }
}