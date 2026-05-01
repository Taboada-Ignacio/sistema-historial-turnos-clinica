package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.request.ProfesionalUpdateDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.ProfesionalMapper;
import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.service.ProfesionalService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Set; // Importante para manejar Set<Rol>
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfesionalServiceImpl implements ProfesionalService {

    private final ProfesionalRepository profesionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final LocalidadRepository localidadRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ProfesionalMapper profesionalMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ProfesionalResponseDTO registrarProfesional(ProfesionalRegistroDTO dto) {
        
        // 1. Validaciones de Identidad
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ReglaDeNegocioException("El correo electrónico " + dto.getEmail() + " ya está en uso.");
        }
        
        if (usuarioRepository.findByDni(dto.getDni()).isPresent()) {
            throw new ReglaDeNegocioException("El DNI " + dto.getDni() + " ya está registrado en el sistema.");
        }

        // 2. Obtención de dependencias (¡NUEVA LÓGICA DE ROLES!)
        // Ahora iteramos sobre los IDs recibidos, pudiendo asignar Profesional, Paciente, o ambos.
        Set<Rol> rolesAsignados = dto.getRolesIds().stream()
                .map(rolId -> rolRepository.findById(rolId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el rol con ID: " + rolId)))
                .collect(Collectors.toSet());

        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la localidad con ID: " + dto.getIdLocalidad()));

        Especialidad especialidad = especialidadRepository.findById(dto.getIdEspecialidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la Especialidad con ID: " + dto.getIdEspecialidad()));

        // 3. Mapeo
        Profesional profesional = profesionalMapper.toEntity(dto);

        // 4. Configuración extra
        profesional.setRoles(rolesAsignados); // CAMBIO: Usamos setRoles en plural
        profesional.setLocalidad(localidad);
        profesional.setEspecialidad(especialidad);
        profesional.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // 5. Guardado
        Profesional profesionalGuardado = profesionalRepository.save(profesional);

        // 6. Devolvemos el DTO
        return profesionalMapper.toResponseDTO(profesionalGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfesionalResponseDTO obtenerProfesionalPorId(Long id) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el profesional con ID: " + id));
        
        return profesionalMapper.toResponseDTO(profesional);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesionalResponseDTO> obtenerTodosLosProfesionales() {
        List<Profesional> profesionales = profesionalRepository.findAll();
        
        return profesionales.stream()
                .map(profesionalMapper::toResponseDTO)
                .collect(Collectors.toList()); 
    }

    @Override
    @Transactional
    public ProfesionalResponseDTO actualizarProfesional(Long id, ProfesionalUpdateDTO dto) {
        
        // 1. Buscamos el profesional existente
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado con ID: " + id));

        // 2. Actualizamos TODOS los datos básicos (heredados de Usuario)
        profesional.setNombre(dto.getNombre());
        profesional.setApellido(dto.getApellido());
        profesional.setDni(dto.getDni());
        profesional.setEmail(dto.getEmail());
        profesional.setTelefono(dto.getTelefono());
        profesional.setFechaNacimiento(dto.getFechaNacimiento());
        profesional.setEstado(dto.getEstado());

        // 3. Actualizamos los datos propios de Profesional
        profesional.setMatricula(dto.getMatricula());

        // 4. Actualizamos las relaciones
        
        // --- Actualizamos Roles ---
        if (dto.getRolesIds() != null && !dto.getRolesIds().isEmpty()) {
            Set<Rol> rolesActualizados = dto.getRolesIds().stream()
                    .map(rolId -> rolRepository.findById(rolId)
                            .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el rol con ID: " + rolId)))
                    .collect(Collectors.toSet());
            profesional.setRoles(rolesActualizados);
        }

        // --- Actualizamos Localidad ---
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada con ID: " + dto.getIdLocalidad()));
        profesional.setLocalidad(localidad);

        // --- Actualizamos Especialidad ---
        Especialidad especialidad = especialidadRepository.findById(dto.getIdEspecialidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada con ID: " + dto.getIdEspecialidad()));
        profesional.setEspecialidad(especialidad);

        // 5. Guardamos los cambios
        profesional = profesionalRepository.save(profesional);

        // 6. Usamos tu mapper directamente para devolver la respuesta aplanada
        return profesionalMapper.toResponseDTO(profesional);
    }

    @Override
    @Transactional 
    public void eliminarSoloProfesional(Long id) {
        // 1. Validamos si existe
        if (!profesionalRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("El profesional con ID " + id + " no fue encontrado.");
        }

        try {
            // 2. Intentamos el borrado físico
            profesionalRepository.deleteById(id);
            
        } catch (DataIntegrityViolationException e) {
            // 3. Manejo de claves foráneas
            throw new ReglaDeNegocioException(
                "No se puede eliminar el profesional con ID " + id + 
                " porque tiene turnos asignados o historiales asociados."
            );
        }
    }
}