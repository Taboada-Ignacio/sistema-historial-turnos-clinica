package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.request.PacienteUpdateDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.PacienteMapper;
import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.service.DireccionService;
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
    
    // Nuevas dependencias para el flujo de estados y confirmaci?n
    private final EstadoRepository estadoRepository;
    private final CambioEstadoRepository cambioEstadoRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    
    private final PacienteMapper pacienteMapper;
    private final PasswordEncoder passwordEncoder;
    private final DireccionService direccionService;

    @Override
    @Transactional
    public PacienteResponseDTO registrarPaciente(PacienteRegistroDTO dto) {
        
        // 1. Validaciones de Identidad
        validarUnicidad(dto.getEmail(), dto.getDni());

        // 2. Obtenci?n de dependencias
        Set<Rol> rolesAsignados = buscarRoles(dto.getRolesIds());
        
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontr? la localidad con ID: " + dto.getIdLocalidad()));

        ObraSocial obraSocial = obraSocialRepository.findById(dto.getIdObraSocial())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontr? la Obra Social con ID: " + dto.getIdObraSocial()));

        // 3. Buscar el estado inicial "PENDIENTE"
        Estado estadoPendiente = estadoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new ReglaDeNegocioException("El estado inicial PENDIENTE no est? configurado en la base de datos."));

        // 4. Mapeo y Configuraci?n Extra
        Paciente paciente = pacienteMapper.toEntity(dto);
        paciente.setRoles(rolesAsignados);
        paciente.setLocalidad(localidad);
        paciente.setDireccion(direccionService.obtenerOCrearPorTextoYLocalidad(dto.getDireccion(), localidad));
        paciente.setObraSocial(obraSocial);
        paciente.setPassword(passwordEncoder.encode(dto.getPassword()));
        paciente.setEstadoActual(estadoPendiente); // Seteamos el objeto Estado
        
        // 5. Guardado del Paciente
        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        // 6. Registrar Auditor?a de Estado
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de confirmaci?n inv?lido o expirado."));

        if (vToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new ReglaDeNegocioException("El link de confirmaci?n ha expirado. Por favor, solicita uno nuevo.");
        }

        // 2. Obtener el estado "ACTIVO"
        Estado estadoActivo = estadoRepository.findByNombre("ACTIVO")
                .orElseThrow(() -> new ReglaDeNegocioException("El estado ACTIVO no est? configurado."));

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
                        "No se encontr? un usuario registrado con el email: " + email));

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
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontr? el paciente con ID: " + id));
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

        // Actualizamos datos b?sicos
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
                    .orElseThrow(() -> new RecursoNoEncontradoException("Estado no v?lido: " + dto.getEstadoActual()));
            
            if (!paciente.getEstadoActual().equals(nuevoEstado)) {
                paciente.setEstadoActual(nuevoEstado);
                registrarCambioEstado(paciente, nuevoEstado);
            }
        }

        // Actualizaci?n de Relaciones
        if (dto.getRolesIds() != null) {
            paciente.setRoles(buscarRoles(dto.getRolesIds()));
        }

        Localidad localidadObjetivo = paciente.getLocalidad();
        if (dto.getIdLocalidad() != null) {
            localidadObjetivo = localidadRepository.findById(dto.getIdLocalidad())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada"));
        }

        if (dto.getDireccion() != null && !dto.getDireccion().isBlank()) {
            paciente.setDireccion(direccionService.obtenerOCrearPorTextoYLocalidad(dto.getDireccion(), localidadObjetivo));
        } else if (dto.getIdLocalidad() != null
                && paciente.getDireccion() != null
                && paciente.getDireccion().getLocalidad() != null
                && !paciente.getDireccion().getLocalidad().getIdLocalidad().equals(localidadObjetivo.getIdLocalidad())) {
            throw new ReglaDeNegocioException(
                    "Si cambi?s la localidad, envi? la direcci?n en texto para esa localidad (o no cambies la localidad).");
        }

        if (dto.getIdLocalidad() != null) {
            paciente.setLocalidad(localidadObjetivo);
        }

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

    // --- METODOS PRIVADOS DE APOYO ---

    private void validarUnicidad(String email, Integer dni) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ReglaDeNegocioException("El correo " + email + " ya esta en uso.");
        }
        if (usuarioRepository.findByDni(dni).isPresent()) {
            throw new ReglaDeNegocioException("El DNI " + dni + " ya esta registrado.");
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