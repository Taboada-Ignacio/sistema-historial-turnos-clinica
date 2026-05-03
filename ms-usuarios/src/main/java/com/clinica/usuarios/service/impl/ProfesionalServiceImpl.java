package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.request.ProfesionalUpdateDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.ProfesionalMapper;
import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.service.EmailService; // Importante
import com.clinica.usuarios.service.ProfesionalService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    
    private final EstadoRepository estadoRepository;
    private final CambioEstadoRepository cambioEstadoRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService; // NUEVA DEPENDENCIA
    
    private final ProfesionalMapper profesionalMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ProfesionalResponseDTO registrarProfesional(ProfesionalRegistroDTO dto) {
        // 1. Validaciones de Identidad
        validarUnicidadProfesional(dto.getEmail(), dto.getDni());

        // 2. Obtención de dependencias
        Set<Rol> rolesAsignados = buscarRoles(dto.getRolesIds());
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada"));
        Especialidad especialidad = especialidadRepository.findById(dto.getIdEspecialidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));

        // 3. Definir estado inicial "PENDIENTE"
        Estado estadoPendiente = estadoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new ReglaDeNegocioException("Estado inicial no configurado"));

        // 4. Mapeo y Configuración
        Profesional profesional = profesionalMapper.toEntity(dto);
        profesional.setRoles(rolesAsignados);
        profesional.setLocalidad(localidad);
        profesional.setEspecialidad(especialidad);
        profesional.setPassword(passwordEncoder.encode(dto.getPassword()));
        profesional.setEstadoActual(estadoPendiente);

        // 5. Guardado
        Profesional profesionalGuardado = profesionalRepository.save(profesional);

        // 6. Auditoría de estado y generación de Token
        registrarHistorialEstado(profesionalGuardado, estadoPendiente);
        String token = crearTokenVerificacion(profesionalGuardado);

        // 7. ENVÍO DE EMAIL (NUEVO)
        emailService.enviarEmailConfirmacion(profesionalGuardado, token);

        return profesionalMapper.toResponseDTO(profesionalGuardado);
    }

    @Override
    @Transactional
    public void confirmarCuenta(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de confirmación inválido o inexistente"));

        if (vToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new ReglaDeNegocioException("El link de confirmación ha expirado.");
        }

        Usuario usuario = vToken.getUsuario();
        Estado estadoActivo = estadoRepository.findByNombre("ACTIVO")
                .orElseThrow(() -> new ReglaDeNegocioException("Estado ACTIVO no disponible"));

        usuario.setEstadoActual(estadoActivo);
        usuarioRepository.save(usuario);
        
        registrarHistorialEstado(usuario, estadoActivo);
        tokenRepository.delete(vToken);
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
        return profesionalRepository.findAll().stream()
                .map(profesionalMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProfesionalResponseDTO actualizarProfesional(Long id, ProfesionalUpdateDTO dto) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        profesional.setNombre(dto.getNombre());
        profesional.setApellido(dto.getApellido());
        profesional.setDni(dto.getDni()); // Agregado para consistencia con Paciente
        profesional.setEmail(dto.getEmail()); // Agregado para consistencia con Paciente
        profesional.setTelefono(dto.getTelefono());
        profesional.setMatricula(dto.getMatricula());

        if (dto.getEstadoActual() != null) {
            Estado nuevoEstado = estadoRepository.findByNombre(dto.getEstadoActual())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Estado solicitado no válido"));
            
            if (!profesional.getEstadoActual().equals(nuevoEstado)) {
                profesional.setEstadoActual(nuevoEstado);
                registrarHistorialEstado(profesional, nuevoEstado);
            }
        }

        return profesionalMapper.toResponseDTO(profesionalRepository.save(profesional));
    }

    @Override
    @Transactional
    public void eliminarSoloProfesional(Long id) {
        if (!profesionalRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("El profesional con ID " + id + " no fue encontrado.");
        }
        try {
            profesionalRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaDeNegocioException(
                "No se puede eliminar el profesional porque tiene turnos asignados o historiales asociados."
            );
        }
    }

    // --- MÉTODOS PRIVADOS AUXILIARES ---

    private void registrarHistorialEstado(Usuario usuario, Estado estado) {
        CambioEstado historial = CambioEstado.builder()
                .usuario(usuario)
                .estado(estado)
                .fecha(LocalDateTime.now())
                .build();
        cambioEstadoRepository.save(historial);
    }

    private String crearTokenVerificacion(Usuario usuario) {
        String tokenString = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.builder()
                .token(tokenString)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusHours(48)) 
                .build();
        tokenRepository.save(token);
        return tokenString; // Retornamos el string para el email
    }

    private void validarUnicidadProfesional(String email, Integer dni) {
        if (usuarioRepository.findByEmail(email).isPresent()) throw new ReglaDeNegocioException("Email en uso.");
        if (usuarioRepository.findByDni(dni).isPresent()) throw new ReglaDeNegocioException("DNI ya registrado.");
    }

    private Set<Rol> buscarRoles(Set<Long> rolesIds) {
        return rolesIds.stream()
                .map(id -> rolRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + id)))
                .collect(Collectors.toSet());
    }
}