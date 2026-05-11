package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.request.PacienteUpdateDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.PacienteMapper;
import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.service.EmailService;
import com.clinica.usuarios.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final LocalidadRepository localidadRepository;
    private final ObraSocialRepository obraSocialRepository;
    
    // Nuevas dependencias para el flujo de estados y confirmación
    private final EstadoRepository estadoRepository;
    private final CambioEstadoRepository cambioEstadoRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    
    private final PacienteMapper pacienteMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public PacienteResponseDTO registrarPaciente(PacienteRegistroDTO dto) {
        
        // 1. Validaciones de Identidad
        validarUnicidad(dto.getEmail(), dto.getDni());

        // 2. Obtención de dependencias
        Set<Rol> rolesAsignados = buscarRoles(dto.getRolesIds());
        
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la localidad con ID: " + dto.getIdLocalidad()));

        ObraSocial obraSocial = obraSocialRepository.findById(dto.getIdObraSocial())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la Obra Social con ID: " + dto.getIdObraSocial()));

        // 3. Buscar el estado inicial "PENDIENTE"
        Estado estadoPendiente = estadoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new ReglaDeNegocioException("El estado inicial PENDIENTE no está configurado en la base de datos."));

        // 4. Mapeo y Configuración Extra
        Paciente paciente = pacienteMapper.toEntity(dto);
        paciente.setRoles(rolesAsignados);
        paciente.setLocalidad(localidad);
        paciente.setObraSocial(obraSocial);
        paciente.setPassword(passwordEncoder.encode(dto.getPassword()));
        paciente.setEstadoActual(estadoPendiente); // Seteamos el objeto Estado
        
        // 5. Guardado del Paciente
        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        // 6. Registrar Auditoría de Estado
        registrarCambioEstado(pacienteGuardado, estadoPendiente);

        // 7. Generar Token y Enviar Correo
        String token = generarToken(pacienteGuardado);
        emailService.enviarEmailConfirmacion(pacienteGuardado, token);

        return pacienteMapper.toResponseDTO(pacienteGuardado);
    }

    @Override
    @Transactional
    public void confirmarCuenta(String token) {
        // 1. Validar el token
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de confirmación inválido o expirado."));

        if (vToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new ReglaDeNegocioException("El link de confirmación ha expirado. Por favor, solicita uno nuevo.");
        }

        // 2. Obtener el estado "ACTIVO"
        Estado estadoActivo = estadoRepository.findByNombre("ACTIVO")
                .orElseThrow(() -> new ReglaDeNegocioException("El estado ACTIVO no está configurado."));

        // 3. Actualizar el usuario
        Usuario usuario = vToken.getUsuario();
        usuario.setEstadoActual(estadoActivo);
        usuarioRepository.save(usuario);

        // 4. Registrar el cambio en el historial
        registrarCambioEstado(usuario, estadoActivo);

        // 5. Eliminar el token (ya fue usado)
        tokenRepository.delete(vToken);
    }

    @Override
    @Transactional
    public void reenviarCorreoConfirmacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un usuario registrado con el email: " + email));

        if (!(usuario instanceof Paciente)) {
            throw new ReglaDeNegocioException("El correo no corresponde a un paciente registrado.");
        }

        if ("ACTIVO".equalsIgnoreCase(usuario.getEstadoActual().getNombre())) {
            throw new ReglaDeNegocioException("La cuenta ya se encuentra activa. No es necesario reenviar el correo.");
        }

        tokenRepository.deleteByUsuario(usuario);
        String nuevoToken = generarToken(usuario);
        emailService.enviarEmailConfirmacion(usuario, nuevoToken);
    }

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
        return pacienteRepository.findAll().stream()
                .map(pacienteMapper::toResponseDTO)
                .collect(Collectors.toList()); 
    }

    @Override
    @Transactional
    public PacienteResponseDTO actualizarPaciente(Long id, PacienteUpdateDTO dto) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado con ID: " + id));

        // Actualizamos datos básicos
        paciente.setNombre(dto.getNombre());
        paciente.setApellido(dto.getApellido());
        paciente.setDni(dto.getDni());
        paciente.setEmail(dto.getEmail());
        paciente.setTelefono(dto.getTelefono());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setNumeroAfiliado(dto.getNumeroAfiliado());

        // Manejo del Cambio de Estado (si viene en el DTO)
        if (dto.getEstadoActual() != null) {
            Estado nuevoEstado = estadoRepository.findByNombre(dto.getEstadoActual())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Estado no válido: " + dto.getEstadoActual()));
            
            if (!paciente.getEstadoActual().equals(nuevoEstado)) {
                paciente.setEstadoActual(nuevoEstado);
                registrarCambioEstado(paciente, nuevoEstado);
            }
        }

        // Actualización de Relaciones
        if (dto.getRolesIds() != null) {
            paciente.setRoles(buscarRoles(dto.getRolesIds()));
        }

        paciente.setLocalidad(localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada")));

        paciente.setObraSocial(obraSocialRepository.findById(dto.getIdObraSocial())
                .orElseThrow(() -> new RecursoNoEncontradoException("Obra Social no encontrada")));

        return pacienteMapper.toResponseDTO(pacienteRepository.save(paciente));
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
            throw new ReglaDeNegocioException("No se puede eliminar el paciente porque tiene registros asociados.");
        }
    }

    // --- MÉTODOS PRIVADOS DE APOYO ---

    private void validarUnicidad(String email, Integer dni) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ReglaDeNegocioException("El correo " + email + " ya está en uso.");
        }
        if (usuarioRepository.findByDni(dni).isPresent()) {
            throw new ReglaDeNegocioException("El DNI " + dni + " ya está registrado.");
        }
    }

    private Set<Rol> buscarRoles(Set<Long> rolesIds) {
        return rolesIds.stream()
                .map(id -> rolRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + id)))
                .collect(Collectors.toSet());
    }

    private void registrarCambioEstado(Usuario usuario, Estado estado) {
        CambioEstado cambio = CambioEstado.builder()
                .usuario(usuario)
                .estado(estado)
                .fecha(LocalDateTime.now())
                .build();
        cambioEstadoRepository.save(cambio);
    }

    private String generarToken(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        VerificationToken vToken = VerificationToken.builder()
                .token(token)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(vToken);
        return token;
    }
}