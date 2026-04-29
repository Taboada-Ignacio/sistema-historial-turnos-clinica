package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.PacienteMapper;
import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.service.PacienteService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

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

    @SuppressWarnings("null") // <--- ESTO ELIMINA LAS ADVERTENCIAS AMARILLAS
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

        // 2. Obtención de dependencias
        Rol rolPaciente = rolRepository.findByDescripcion("PACIENTE")  
                .orElseThrow(() -> new RecursoNoEncontradoException("Error interno: El rol 'PACIENTE' no existe."));

        // Aquí es donde saltaba el aviso porque findById espera un @NonNull
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la localidad con ID: " + dto.getIdLocalidad()));

        ObraSocial obraSocial = obraSocialRepository.findById(dto.getIdObraSocial())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la Obra Social con ID: " + dto.getIdObraSocial()));

        // 3. Mapeo
        Paciente paciente = pacienteMapper.toEntity(dto);

        // 4. Configuración extra
        paciente.setRol(rolPaciente);
        paciente.setLocalidad(localidad);
        paciente.setObraSocial(obraSocial);
        paciente.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // 5. Guardado
        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        return pacienteMapper.toResponseDTO(pacienteGuardado);
    }

    // --- NUEVOS MÉTODOS CRUD (READ) ---

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO obtenerPacientePorId(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el paciente con ID: " + id));
        
        // Mapeamos la entidad encontrada al DTO de respuesta
        return pacienteMapper.toResponseDTO(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> obtenerTodosLosPacientes() {
        List<Paciente> pacientes = pacienteRepository.findAll();
        
        // Usamos la API de Streams de Java para transformar la lista de Entidades a lista de DTOs
        return pacientes.stream()
                .map(pacienteMapper::toResponseDTO)
                .collect(Collectors.toList()); 
        // Nota: Si estás usando Java 16 o superior, puedes usar .toList() en lugar de .collect(Collectors.toList())
    }
}