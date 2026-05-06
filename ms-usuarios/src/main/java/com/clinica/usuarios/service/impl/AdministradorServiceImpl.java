package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.request.AdministradorUpdateDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.AdministradorMapper;
import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.service.AdministradorService;
import com.clinica.usuarios.service.EmailService; // Importado
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class AdministradorServiceImpl implements AdministradorService {

    @Value("${system.registration.secret}")
    private String systemSecret;

    private final AdministradorRepository administradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final LocalidadRepository localidadRepository;
    private final EstadoRepository estadoRepository;
    private final CambioEstadoRepository cambioEstadoRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService; // NUEVA DEPENDENCIA
    private final AdministradorMapper administradorMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AdministradorResponseDTO registrarAdministrador(AdministradorRegistroDTO dto, String providedSecret) {
        
        // 1. Validar Clave del Sistema (Seguridad de infraestructura)
        if (providedSecret == null || !providedSecret.equals(systemSecret)) {
            throw new ReglaDeNegocioException("Acceso denegado: La clave del sistema es incorrecta.");
        }

        // 2. Validar Unicidad de Email y DNI
        validarUnicidadUsuario(dto.getEmail(), dto.getDni());

        // 3. Obtención de dependencias y Estado inicial
        Rol rolAdmin = obtenerRolObligatorio("ROLE_ADMINISTRADOR");
        Rol rolProfesional = obtenerRolObligatorio("ROLE_PROFESIONAL");
        Rol rolPaciente = obtenerRolObligatorio("ROLE_PACIENTE");
        
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada con ID: " + dto.getIdLocalidad()));

        Estado estadoPendiente = estadoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new ReglaDeNegocioException("Estado inicial PENDIENTE no configurado"));

        // 4. Mapeo y Configuración
        Administrador admin = administradorMapper.toEntity(dto);
        admin.setRoles(Set.of(rolAdmin, rolProfesional, rolPaciente));
        admin.setLocalidad(localidad);
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setEstadoActual(estadoPendiente); 

        // 5. Guardado del Administrador
        Administrador adminGuardado = administradorRepository.save(admin);

        // 6. Registro de Auditoría, Generación de Token y ENVÍO DE EMAIL
        registrarCambioEstado(adminGuardado, estadoPendiente);
        String token = generarTokenVerificacion(adminGuardado); // Ahora devuelve el String
        
        // Disparamos el correo asíncrono
        emailService.enviarEmailConfirmacion(adminGuardado, token);

        return administradorMapper.toResponseDTO(adminGuardado);
    }

    @Override
    @Transactional
    public void confirmarCuenta(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de confirmación inválido"));

        if (vToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new ReglaDeNegocioException("El link de confirmación ha expirado.");
        }

        Usuario usuario = vToken.getUsuario();
        Estado estadoActivo = estadoRepository.findByNombre("ACTIVO")
                .orElseThrow(() -> new ReglaDeNegocioException("Estado ACTIVO no disponible"));

        usuario.setEstadoActual(estadoActivo);
        usuarioRepository.save(usuario);
        registrarCambioEstado(usuario, estadoActivo);

        tokenRepository.delete(vToken);
    }

    @Override
    @Transactional(readOnly = true)
    public AdministradorResponseDTO obtenerAdministradorPorId(Long id) {
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado con ID: " + id));
        return administradorMapper.toResponseDTO(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministradorResponseDTO> obtenerTodosLosAdministradores() {
        return administradorRepository.findAll().stream()
                .map(administradorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdministradorResponseDTO actualizarAdministrador(Long id, AdministradorUpdateDTO dto) {
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado"));

        admin.setNombre(dto.getNombre());
        admin.setApellido(dto.getApellido());
        admin.setDni(dto.getDni());
        admin.setEmail(dto.getEmail());
        admin.setTelefono(dto.getTelefono());
        admin.setFechaNacimiento(dto.getFechaNacimiento());

        if (dto.getEstadoActual() != null) {
            Estado nuevoEstado = estadoRepository.findByNombre(dto.getEstadoActual())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Estado no válido: " + dto.getEstadoActual()));
            
            if (!admin.getEstadoActual().equals(nuevoEstado)) {
                admin.setEstadoActual(nuevoEstado);
                registrarCambioEstado(admin, nuevoEstado);
            }
        }

        return administradorMapper.toResponseDTO(administradorRepository.save(admin));
    }

    @Override
    @Transactional
    public void eliminarAdministrador(Long id) {
        if (!administradorRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Administrador no encontrado");
        }
        try {
            administradorRepository.deleteById(id);
        } catch (Exception e) {
            throw new ReglaDeNegocioException("Error al eliminar administrador: posee registros asociados.");
        }
    }

    // --- MÉTODOS PRIVADOS DE APOYO ---

    private void registrarCambioEstado(Usuario usuario, Estado estado) {
        CambioEstado historial = CambioEstado.builder()
                .usuario(usuario)
                .estado(estado)
                .fecha(LocalDateTime.now())
                .build();
        cambioEstadoRepository.save(historial);
    }

    private String generarTokenVerificacion(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        VerificationToken vToken = VerificationToken.builder()
                .token(token)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusHours(12)) 
                .build();
        tokenRepository.save(vToken);
        return token; // Retornamos el token para el EmailService
    }

    private void validarUnicidadUsuario(String email, Integer dni) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ReglaDeNegocioException("El correo electrónico ya está en uso.");
        }
        if (usuarioRepository.findByDni(dni).isPresent()) {
            throw new ReglaDeNegocioException("El DNI ya está registrado.");
        }
    }

    private Rol obtenerRolObligatorio(String descripcionRol) {
        return rolRepository.findByDescripcion(descripcionRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol " + descripcionRol + " no encontrado"));
    }
}