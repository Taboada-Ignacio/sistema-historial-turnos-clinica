package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.request.AdministradorUpdateDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.AdministradorMapper;
import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.service.AccountActivationService;
import com.clinica.usuarios.service.AdministradorService;
import com.clinica.usuarios.service.DireccionService;
import com.clinica.usuarios.service.EmailService;
import com.clinica.usuarios.service.VerificationTokenService;
import com.clinica.usuarios.service.support.UnicidadUsuarioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenService verificationTokenService;
    private final AccountActivationService accountActivationService;
    private final EmailService emailService;
    private final AdministradorMapper administradorMapper;
    private final PasswordEncoder passwordEncoder;
    private final DireccionService direccionService;

    @Override
    @Transactional
    public AdministradorResponseDTO registrarAdministrador(AdministradorRegistroDTO dto, String providedSecret) {
        
        // 1. Validar Clave del Sistema (Seguridad de infraestructura)
        if (providedSecret == null || !providedSecret.equals(systemSecret)) {
            throw new ReglaDeNegocioException("Acceso denegado: La clave del sistema es incorrecta.");
        }

        // 2. Validar Unicidad de Email y DNI
        UnicidadUsuarioValidator.validarAlta(usuarioRepository, dto.getEmail(), dto.getDni());

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
        admin.setDireccion(direccionService.obtenerOCrearPorTextoYLocalidad(dto.getDireccion(), localidad));
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setEstadoActual(estadoPendiente); 

        // 5. Guardado del Administrador
        Administrador adminGuardado = administradorRepository.save(admin);

        // 6. Registro de Auditoría, Generación de Token y ENVÍO DE EMAIL
        registrarCambioEstado(adminGuardado, estadoPendiente);
        VerificationTokenService.DatosConfirmacion datos = verificationTokenService.crearTokenConfirmacion(adminGuardado);
        emailService.enviarEmailConfirmacion(adminGuardado, datos.token(), datos.codigo());

        return administradorMapper.toResponseDTO(adminGuardado);
    }

    @Override
    @Transactional
    public void confirmarCuenta(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de confirmación inválido"));
        accountActivationService.confirmarCuentaDesdeToken(vToken, this::registrarHistorialTrasActivacion);
    }

    @Override
    @Transactional
    public void confirmarCuentaConCodigo(String email, String codigo) {
        VerificationToken vToken = tokenRepository.findByUsuario_EmailAndCodigo(email, codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Código de confirmación inválido."));

        Usuario usuario = vToken.getUsuario();
        if (!(usuario instanceof Administrador)) {
            throw new ReglaDeNegocioException("El correo no corresponde a un administrador registrado.");
        }

        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMINISTRADOR".equals(r.getDescripcion()));
        if (!esAdmin) {
            throw new ReglaDeNegocioException("El correo no corresponde a un administrador registrado.");
        }

        accountActivationService.confirmarCuentaDesdeToken(vToken, this::registrarHistorialTrasActivacion);
    }

    @Override
    @Transactional
    public void reenviarCorreoConfirmacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un usuario registrado con el email: " + email));

        if (!(usuario instanceof Administrador)) {
            throw new ReglaDeNegocioException("El correo no corresponde a un administrador registrado.");
        }

        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMINISTRADOR".equals(r.getDescripcion()));
        if (!esAdmin) {
            throw new ReglaDeNegocioException("El correo no corresponde a un administrador registrado.");
        }

        if ("ACTIVO".equalsIgnoreCase(usuario.getEstadoActual().getNombre())) {
            throw new ReglaDeNegocioException("La cuenta ya se encuentra activa. No es necesario reenviar el correo.");
        }

        VerificationTokenService.DatosConfirmacion datos = verificationTokenService.crearTokenConfirmacion(usuario);
        emailService.enviarEmailConfirmacion(usuario, datos.token(), datos.codigo());
    }

    @Override
    @Transactional(readOnly = true)
    public AdministradorResponseDTO obtenerAdministradorPorId(Long id) {
        Administrador admin = administradorRepository.findWithUbicacionById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado con ID: " + id));
        return administradorMapper.toResponseDTO(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public AdministradorResponseDTO obtenerAdministradorPorEmail(String email) {
        Administrador admin = administradorRepository.findByEmail(email)
                .flatMap(a -> administradorRepository.findWithUbicacionById(a.getIdUsuario()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado."));
        return administradorMapper.toResponseDTO(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministradorResponseDTO> obtenerTodosLosAdministradores() {
        return administradorRepository.findAll().stream()
                .sorted(Comparator.comparing(Administrador::getApellido, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(Administrador::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(administradorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdministradorResponseDTO actualizarAdministrador(Long id, AdministradorUpdateDTO dto) {
        Administrador admin = administradorRepository.findWithUbicacionById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado"));

        UnicidadUsuarioValidator.validarActualizacion(
                usuarioRepository, admin.getIdUsuario(), dto.getEmail(), dto.getDni());

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

        if (dto.getIdLocalidad() != null || (dto.getDireccion() != null && !dto.getDireccion().isBlank())) {
            Localidad localidadObjetivo = dto.getIdLocalidad() != null
                    ? localidadRepository.findById(dto.getIdLocalidad())
                            .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada"))
                    : admin.getDireccion().getLocalidad();

            if (dto.getDireccion() != null && !dto.getDireccion().isBlank()) {
                admin.setDireccion(direccionService.obtenerOCrearPorTextoYLocalidad(dto.getDireccion(), localidadObjetivo));
            } else if (dto.getIdLocalidad() != null
                    && admin.getDireccion() != null
                    && admin.getDireccion().getLocalidad() != null
                    && !admin.getDireccion().getLocalidad().getIdLocalidad().equals(localidadObjetivo.getIdLocalidad())) {
                throw new ReglaDeNegocioException(
                        "Si cambiás la localidad, enviá la dirección en texto para esa localidad.");
            }
        }

        return administradorMapper.toResponseDTO(administradorRepository.save(admin));
    }

    @Override
    @Transactional
    public void eliminarAdministrador(Long id) {
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado"));
        tokenRepository.deleteByUsuario(admin);
        refreshTokenRepository.deleteByUsuario(admin);
        cambioEstadoRepository.deleteByUsuario_IdUsuario(id);
        try {
            administradorRepository.delete(admin);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaDeNegocioException("No se puede eliminar el administrador porque tiene registros asociados.");
        }
    }

    // --- MÉTODOS PRIVADOS DE APOYO ---

    private void registrarHistorialTrasActivacion(Usuario usuario) {
        registrarCambioEstado(usuario, usuario.getEstadoActual());
    }

    private void registrarCambioEstado(Usuario usuario, Estado estado) {
        CambioEstado historial = CambioEstado.builder()
                .usuario(usuario)
                .estado(estado)
                .fecha(LocalDateTime.now())
                .build();
        cambioEstadoRepository.save(historial);
    }

    private Rol obtenerRolObligatorio(String descripcionRol) {
        return rolRepository.findByDescripcion(descripcionRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol " + descripcionRol + " no encontrado"));
    }
}