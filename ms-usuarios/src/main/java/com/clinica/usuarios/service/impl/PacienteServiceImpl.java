package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.request.PacienteUpdateDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.PacienteMapper;
import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.service.PacienteService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final LocalidadRepository localidadRepository;
    private final ObraSocialRepository obraSocialRepository;
    private final PacienteMapper pacienteMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public PacienteResponseDTO registrarPaciente(PacienteRegistroDTO dto) {
        
        // 1. Validaciones de Identidad
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ReglaDeNegocioException("El correo electrónico " + dto.getEmail() + " ya está en uso.");
        }
        
        if (usuarioRepository.findByDni(dto.getDni()).isPresent()) {
            throw new ReglaDeNegocioException("El DNI " + dto.getDni() + " ya está registrado en el sistema.");
        }

        // 2. Obtención de dependencias (¡NUEVA LÓGICA DE ROLES!)
        Set<Rol> rolesAsignados = dto.getRolesIds().stream()
                .map(rolId -> rolRepository.findById(rolId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el rol con ID: " + rolId)))
                .collect(Collectors.toSet());

        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la localidad con ID: " + dto.getIdLocalidad()));

        ObraSocial obraSocial = obraSocialRepository.findById(dto.getIdObraSocial())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la Obra Social con ID: " + dto.getIdObraSocial()));

        // 3. Mapeo
        Paciente paciente = pacienteMapper.toEntity(dto);

        // 4. Configuración extra
        paciente.setRoles(rolesAsignados); // Usamos setRoles en plural
        paciente.setLocalidad(localidad);
        paciente.setObraSocial(obraSocial);
        paciente.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // 5. Guardado
        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        return pacienteMapper.toResponseDTO(pacienteGuardado);
    }

    // --- MÉTODOS CRUD (READ) ---

    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO obtenerPacientePorId(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el paciente con ID: " + id));
        
        return pacienteMapper.toResponseDTO(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> obtenerTodosLosPacientes() {
        List<Paciente> pacientes = pacienteRepository.findAll();
        
        return pacientes.stream()
                .map(pacienteMapper::toResponseDTO)
                .collect(Collectors.toList()); 
    }

    @Override
    @Transactional
    public PacienteResponseDTO actualizarPaciente(Long id, PacienteUpdateDTO dto) {
        
        // 1. Buscamos el paciente existente
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado con ID: " + id));

        // 2. Actualizamos TODOS los datos básicos (heredados de Usuario)
        paciente.setNombre(dto.getNombre());
        paciente.setApellido(dto.getApellido());
        paciente.setDni(dto.getDni());
        paciente.setEmail(dto.getEmail());
        paciente.setTelefono(dto.getTelefono());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setEstado(dto.getEstado());

        // 3. Actualizamos los datos propios de Paciente
        paciente.setNumeroAfiliado(dto.getNumeroAfiliado());

        // 4. Actualizamos las relaciones (Roles, Localidad y Obra Social)
        
        // --- Actualizamos Roles ---
        if (dto.getRolesIds() != null && !dto.getRolesIds().isEmpty()) {
            Set<Rol> rolesActualizados = dto.getRolesIds().stream()
                    .map(rolId -> rolRepository.findById(rolId)
                            .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el rol con ID: " + rolId)))
                    .collect(Collectors.toSet());
            paciente.setRoles(rolesActualizados);
        }

        // --- Actualizamos Localidad ---
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada con ID: " + dto.getIdLocalidad()));
        paciente.setLocalidad(localidad);

        // --- Actualizamos Obra Social ---
        ObraSocial obraSocial = obraSocialRepository.findById(dto.getIdObraSocial())
                .orElseThrow(() -> new RecursoNoEncontradoException("Obra Social no encontrada con ID: " + dto.getIdObraSocial()));
        paciente.setObraSocial(obraSocial);

        // 5. Guardamos los cambios
        paciente = pacienteRepository.save(paciente);

        // 6. Retornamos usando el Mapper (¡mucho más limpio y a prueba de errores!)
        return pacienteMapper.toResponseDTO(paciente);
    }

    @Override
    @Transactional 
    public void eliminarSoloPaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("El paciente con ID " + id + " no fue encontrado.");
        }

        try {
            pacienteRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaDeNegocioException(
                "No se puede eliminar el paciente con ID " + id + 
                " porque tiene turnos o historial clínico asociado."
            );
        }
    }
}