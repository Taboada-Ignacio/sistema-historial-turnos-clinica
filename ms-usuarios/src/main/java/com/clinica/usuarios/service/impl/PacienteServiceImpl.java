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

    @Override
    @Transactional
    public PacienteResponseDTO actualizarPaciente(Long id, PacienteUpdateDTO dto) {
        
        // 1. Buscamos el paciente existente
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));

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

        // 4. Actualizamos las relaciones (Localidad y Obra Social)
        // Usamos findById porque necesitamos extraer sus nombres más abajo para el DTO
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RuntimeException("Localidad no encontrada con ID: " + dto.getIdLocalidad()));
        paciente.setLocalidad(localidad);

        ObraSocial obraSocial = obraSocialRepository.findById(dto.getIdObraSocial())
                .orElseThrow(() -> new RuntimeException("Obra Social no encontrada con ID: " + dto.getIdObraSocial()));
        paciente.setObraSocial(obraSocial);

        // 5. Guardamos los cambios
        // (JPA actualiza la tabla pacientes y usuarios automáticamente)
        paciente = pacienteRepository.save(paciente);

        // 6. Construimos el DTO aplanado usando el patrón Builder
        return PacienteResponseDTO.builder()
                .idUsuario(paciente.getIdUsuario()) // O paciente.getId(), según cómo lo hayas nombrado
                .nombre(paciente.getNombre())
                .apellido(paciente.getApellido())
                .dni(paciente.getDni())
                .email(paciente.getEmail())
                .telefono(paciente.getTelefono())
                .fechaNacimiento(paciente.getFechaNacimiento())
                .estado(paciente.getEstado())
                .numeroAfiliado(paciente.getNumeroAfiliado())
                // Extraemos los strings para aplanar las relaciones:
                .nombreObraSocial(paciente.getObraSocial().getDescripcion()) // Asegurate de que el atributo se llame nombre en ObraSocial
                .nombreLocalidad(paciente.getLocalidad().getNombre())   // Lo mismo para Localidad
                .nombreProvincia(paciente.getLocalidad().getProvincia().getNombre()) // Y para Provincia
                .build();
    }
}