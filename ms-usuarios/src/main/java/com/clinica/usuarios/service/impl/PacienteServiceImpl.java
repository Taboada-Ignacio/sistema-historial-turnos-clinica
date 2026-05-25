package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.PacienteCargaProfesionalDTO;
import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.request.PacienteUpdateDTO;
import com.clinica.usuarios.dto.response.PacienteActivacionDatosDTO;
import com.clinica.usuarios.dto.response.PacienteBusquedaResponseDTO;
import com.clinica.usuarios.dto.response.PacienteCargaProfesionalResponseDTO;
import com.clinica.usuarios.dto.response.PacienteListadoDTO;
import com.clinica.usuarios.dto.response.PacientePortalSesionDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.model.EstadoUsuario;
import com.clinica.usuarios.repository.PacienteSpecifications;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.PacienteMapper;
import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.service.AccountActivationService;
import com.clinica.usuarios.service.DireccionService;
import com.clinica.usuarios.service.EmailService;
import com.clinica.usuarios.service.PacienteService;
import com.clinica.usuarios.service.VerificationTokenService;
import com.clinica.usuarios.service.support.CuentaEntidadHelper;
import com.clinica.usuarios.service.support.EntidadPortalHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    public static final String CODE_PERFIL_PACIENTE_NO_DISPONIBLE = "PERFIL_PACIENTE_NO_DISPONIBLE";
    public static final String TIPO_CUENTA_PACIENTE = "PACIENTE";
    public static final String TIPO_CUENTA_PROFESIONAL_EN_PORTAL_PACIENTE = "PROFESIONAL_EN_PORTAL_PACIENTE";

    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuentaEntidadHelper cuentaEntidadHelper;
    private final EntidadPortalHelper entidadPortalHelper;
    private final RolRepository rolRepository;
    private final LocalidadRepository localidadRepository;
    private final ProvinciaRepository provinciaRepository;
    private final ObraSocialRepository obraSocialRepository;
    
    // Nuevas dependencias para el flujo de estados y confirmaci?n
    private final EstadoRepository estadoRepository;
    private final CambioEstadoRepository cambioEstadoRepository;
    private final VerificationTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenService verificationTokenService;
    private final AccountActivationService accountActivationService;
    private final EmailService emailService;
    
    private final PacienteMapper pacienteMapper;
    private final PasswordEncoder passwordEncoder;
    private final DireccionService direccionService;

    @Value("${app.url}")
    private String appUrl;

    @Override
    @Transactional
    public PacienteResponseDTO registrarPaciente(PacienteRegistroDTO dto) {
        
        // 1. Validaciones de Identidad
        cuentaEntidadHelper.validarAlta(dto.getEmail(), dto.getDni());

        Set<Rol> rolesAsignados = buscarRoles(dto.getRolesIds());
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontr? la localidad con ID: " + dto.getIdLocalidad()));
        ObraSocial obraSocial = obraSocialRepository.findById(dto.getIdObraSocial())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontr? la Obra Social con ID: " + dto.getIdObraSocial()));
        Estado estadoPendiente = estadoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new ReglaDeNegocioException("El estado inicial PENDIENTE no est? configurado en la base de datos."));

        Paciente paciente = pacienteMapper.toEntity(dto);
        paciente.setDireccion(direccionService.obtenerOCrearPorTextoYLocalidad(dto.getDireccion(), localidad));
        paciente.setObraSocial(obraSocial);
        Paciente pacienteGuardado = cuentaEntidadHelper.guardarPaciente(
                paciente, dto.getEmail(), passwordEncoder.encode(dto.getPassword()), estadoPendiente, rolesAsignados);

        registrarCambioEstado(pacienteGuardado.getUsuario(), estadoPendiente);
        VerificationTokenService.DatosConfirmacion datos =
                verificationTokenService.crearTokenConfirmacion(pacienteGuardado.getUsuario());
        emailService.enviarEmailConfirmacion(pacienteGuardado.getUsuario(), datos.token(), datos.codigo());

        return pacienteMapper.toResponseDTO(pacienteGuardado);
    }

    @Override
    @Transactional
    public void confirmarCuenta(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de confirmación inválido o expirado."));
        accountActivationService.confirmarCuentaDesdeToken(vToken, this::registrarHistorialTrasActivacion);
    }

    @Override
    @Transactional
    public void confirmarCuentaConCodigo(String email, String codigo) {
        VerificationToken vToken = tokenRepository.findByUsuario_EmailAndCodigo(email, codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Código de confirmación inválido."));

        if (!entidadPortalHelper.esPaciente(vToken.getUsuario())) {
            throw new ReglaDeNegocioException("El correo no corresponde a un paciente registrado.");
        }

        accountActivationService.confirmarCuentaDesdeToken(vToken, this::registrarHistorialTrasActivacion);
    }

    @Override
    @Transactional
    public void reenviarCorreoConfirmacion(String email) {
        Paciente paciente = obtenerPacientePorEmail(email);
        String estado = CuentaEntidadHelper.nombreEstado(paciente);
        if (!EstadoUsuario.PENDIENTE.equalsIgnoreCase(estado)) {
            if (EstadoUsuario.SIN_CONTRASENA.equalsIgnoreCase(estado)) {
                throw new ReglaDeNegocioException(
                        "Tu cuenta fue registrada por un profesional. Usá «Reenviar correo de confirmación o activación» en el login.");
            }
            if (EstadoUsuario.ACTIVO.equalsIgnoreCase(estado)) {
                throw new ReglaDeNegocioException("La cuenta ya se encuentra activa. No es necesario reenviar el correo.");
            }
            validarNoBloqueado(paciente);
        }
        enviarCorreoConfirmacionRegistro(paciente);
    }

    @Override
    @Transactional
    public PacienteCargaProfesionalResponseDTO cargarPacienteSinPassword(PacienteCargaProfesionalDTO dto) {
        cuentaEntidadHelper.validarAlta(dto.getEmail(), dto.getDni());

        Rol rolPaciente = rolRepository.findByDescripcion("ROLE_PACIENTE")
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol ROLE_PACIENTE no configurado."));

        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la localidad con ID: " + dto.getIdLocalidad()));

        ObraSocial obraSocial = obraSocialRepository.findById(dto.getIdObraSocial())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la Obra Social con ID: " + dto.getIdObraSocial()));

        Estado sinContrasena = estadoRepository.findByNombre(EstadoUsuario.SIN_CONTRASENA)
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "El estado SIN_CONTRASENA no está configurado en la base de datos."));

        Paciente paciente = Paciente.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .dni(dto.getDni())
                .telefono(dto.getTelefono())
                .fechaNacimiento(dto.getFechaNacimiento())
                .sexo(CuentaEntidadHelper.sexoAsString(dto.getSexo()))
                .direccion(direccionService.obtenerOCrearPorTextoYLocalidad(dto.getDireccion(), localidad))
                .obraSocial(obraSocial)
                .numeroAfiliado(dto.getNumeroAfiliado())
                .build();

        Paciente guardado = cuentaEntidadHelper.guardarPaciente(
                paciente, dto.getEmail(), passwordEncoder.encode(UUID.randomUUID().toString()), sinContrasena, Set.of(rolPaciente));
        registrarCambioEstado(guardado.getUsuario(), sinContrasena);
        enviarCorreoActivacionPaciente(guardado);

        return PacienteCargaProfesionalResponseDTO.builder()
                .idUsuario(guardado.getIdUsuario())
                .mensaje("Paciente cargado. Se envió un correo para que active su cuenta y cree su contraseña.")
                .build();
    }

    @Override
    @Transactional
    public void reenviarCorreoAccesoPaciente(String email) {
        Paciente paciente = obtenerPacientePorEmail(email);
        String estado = CuentaEntidadHelper.nombreEstado(paciente);
        if (EstadoUsuario.BLOQUEADO.equalsIgnoreCase(estado)) {
            throw new ReglaDeNegocioException(
                    "Tu cuenta está bloqueada. Contactá a la clínica o al administrador para rehabilitar el acceso.");
        }
        if (EstadoUsuario.ACTIVO.equalsIgnoreCase(estado)) {
            throw new ReglaDeNegocioException(
                    "Tu cuenta ya está activa. Si olvidaste tu contraseña, usá «¿Olvidaste tu contraseña?» en el login.");
        }
        if (EstadoUsuario.PENDIENTE.equalsIgnoreCase(estado)) {
            enviarCorreoConfirmacionRegistro(paciente);
            return;
        }
        if (EstadoUsuario.SIN_CONTRASENA.equalsIgnoreCase(estado)) {
            enviarCorreoActivacionPaciente(paciente);
            return;
        }
        throw new ReglaDeNegocioException("No se puede reenviar el correo para el estado actual de la cuenta.");
    }

    @Override
    @Transactional
    public void solicitarCambioPasswordPaciente(String email) {
        Paciente paciente = obtenerPacientePorEmail(email);
        String estado = CuentaEntidadHelper.nombreEstado(paciente);
        if (EstadoUsuario.BLOQUEADO.equalsIgnoreCase(estado)) {
            throw new ReglaDeNegocioException(
                    "Tu cuenta está bloqueada. Contactá a la clínica o al administrador para rehabilitar el acceso.");
        }
        if (EstadoUsuario.PENDIENTE.equalsIgnoreCase(estado)) {
            enviarCorreoConfirmacionRegistro(paciente);
            return;
        }
        if (EstadoUsuario.SIN_CONTRASENA.equalsIgnoreCase(estado)) {
            enviarCorreoActivacionPaciente(paciente);
            return;
        }
        if (EstadoUsuario.ACTIVO.equalsIgnoreCase(estado)) {
            enviarCorreoRecuperacionPassword(paciente);
            return;
        }
        throw new ReglaDeNegocioException("No se puede procesar la solicitud para el estado actual de la cuenta.");
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteActivacionDatosDTO obtenerDatosActivacionPaciente(String token) {
        Paciente paciente = resolverPacienteDesdeTokenActivacion(token);
        return toActivacionDatosDTO(paciente);
    }

    @Override
    @Transactional
    public void establecerPasswordInicialPaciente(String token, String passwordNueva) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Este enlace ya fue utilizado o no es válido."));

        if (vToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(vToken);
            throw new ReglaDeNegocioException(
                    "El enlace de activación ha expirado (válido por 72 horas). Solicitá uno nuevo desde el login.");
        }

        Paciente paciente = pacienteRepository.findByUsuario_IdUsuario(vToken.getUsuario().getIdUsuario())
                .orElseThrow(() -> new ReglaDeNegocioException("El enlace no corresponde a un paciente."));

        if (!EstadoUsuario.SIN_CONTRASENA.equalsIgnoreCase(CuentaEntidadHelper.nombreEstado(paciente))) {
            throw new ReglaDeNegocioException("Esta cuenta no está pendiente de activación con contraseña.");
        }

        Estado activo = estadoRepository.findByNombre(EstadoUsuario.ACTIVO)
                .orElseThrow(() -> new ReglaDeNegocioException("Estado ACTIVO no disponible"));

        Usuario usuario = paciente.getUsuario();
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuario.setEstadoActual(activo);
        usuarioRepository.save(usuario);
        registrarCambioEstado(usuario, activo);
        tokenRepository.delete(vToken);
        refreshTokenRepository.deleteByUsuario(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO obtenerPacientePorId(Long id) {
        Paciente paciente = pacienteRepository.findWithUbicacionById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontr? el paciente con ID: " + id));
        return pacienteMapper.toResponseDTO(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public PacientePortalSesionDTO obtenerPacienteSesion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

        if (entidadPortalHelper.esPaciente(usuario)) {
            return pacienteRepository.findWithUbicacionById(usuario.getIdUsuario())
                    .map(p -> toPortalSesionDesdePaciente(pacienteMapper.toResponseDTO(p)))
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No se encontró el paciente con ID: " + usuario.getIdUsuario()));
        }

        if (entidadPortalHelper.esProfesional(usuario)) {
            if (!EntidadPortalHelper.tieneRol(usuario, "ROLE_PACIENTE")) {
                throw new ReglaDeNegocioException("La cuenta no corresponde a un paciente.");
            }
            Profesional profesional = profesionalRepository.findWithUbicacionById(usuario.getIdUsuario())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));
            return PacientePortalSesionDTO.builder()
                    .idUsuario(profesional.getIdUsuario())
                    .nombre(profesional.getNombre())
                    .apellido(profesional.getApellido())
                    .email(CuentaEntidadHelper.emailDe(profesional))
                    .telefono(profesional.getTelefono())
                    .dni(profesional.getDni())
                    .fechaNacimiento(profesional.getFechaNacimiento())
                    .sexo(CuentaEntidadHelper.sexoAsEnum(profesional.getSexo()))
                    .estadoActual(usuario.getEstadoActual() != null
                            ? usuario.getEstadoActual().getNombre() : null)
                    .roles(rolesADescripciones(usuario.getRoles()))
                    .tipoCuenta(TIPO_CUENTA_PROFESIONAL_EN_PORTAL_PACIENTE)
                    .perfilEditable(false)
                    .build();
        }

        throw new ReglaDeNegocioException("La cuenta no corresponde a un paciente.");
    }

    private PacientePortalSesionDTO toPortalSesionDesdePaciente(PacienteResponseDTO p) {
        return PacientePortalSesionDTO.builder()
                .idUsuario(p.getIdUsuario())
                .nombre(p.getNombre())
                .apellido(p.getApellido())
                .dni(p.getDni())
                .email(p.getEmail())
                .telefono(p.getTelefono())
                .fechaNacimiento(p.getFechaNacimiento())
                .sexo(p.getSexo())
                .estadoActual(p.getEstadoActual())
                .idObraSocial(p.getIdObraSocial())
                .nombreObraSocial(p.getNombreObraSocial())
                .numeroAfiliado(p.getNumeroAfiliado())
                .idLocalidad(p.getIdLocalidad())
                .idProvincia(p.getIdProvincia())
                .nombreLocalidad(p.getNombreLocalidad())
                .nombreProvincia(p.getNombreProvincia())
                .direccion(p.getDireccion())
                .roles(p.getRoles())
                .tipoCuenta(TIPO_CUENTA_PACIENTE)
                .perfilEditable(true)
                .build();
    }

    private static Set<String> rolesADescripciones(Set<Rol> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream().map(Rol::getDescripcion).collect(Collectors.toSet());
    }

    private static boolean tieneRol(Usuario usuario, String rol) {
        return usuario.getRoles() != null
                && usuario.getRoles().stream().anyMatch(r -> rol.equals(r.getDescripcion()));
    }

    private void rechazarEdicionPerfilSiProfesional(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + idUsuario));
        if (entidadPortalHelper.esProfesional(usuario)) {
            throw new ReglaDeNegocioException(
                    "Tu perfil se gestiona desde el portal profesional. Ingresá como profesional para editar tus datos.",
                    CODE_PERFIL_PACIENTE_NO_DISPONIBLE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteBusquedaResponseDTO buscarPacientes(String texto, Long idProvincia, Long idLocalidad) {
        boolean tieneTexto = texto != null && !texto.isBlank();
        boolean tieneProvincia = idProvincia != null;
        boolean tieneLocalidad = idLocalidad != null;

        if (!tieneTexto && !tieneProvincia) {
            throw new ReglaDeNegocioException(
                    "Indicá al menos apellido/nombre o una provincia para buscar pacientes.");
        }
        if (tieneLocalidad && !tieneProvincia) {
            throw new ReglaDeNegocioException("Para filtrar por localidad debés seleccionar también la provincia.");
        }

        String nombreProvincia = null;
        String nombreLocalidad = null;
        if (tieneLocalidad) {
            Localidad localidad = localidadRepository.findById(idLocalidad)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada."));
            if (!localidad.getProvincia().getIdProvincia().equals(idProvincia)) {
                throw new ReglaDeNegocioException("La localidad no pertenece a la provincia seleccionada.");
            }
            nombreLocalidad = localidad.getNombre();
            nombreProvincia = localidad.getProvincia().getNombre();
        } else if (tieneProvincia) {
            nombreProvincia = provinciaRepository.findById(idProvincia)
                    .map(Provincia::getNombre)
                    .orElse("provincia seleccionada");
        }

        String textoNorm = tieneTexto ? texto.trim() : null;
        Specification<Paciente> spec = PacienteSpecifications.busquedaAdmin(textoNorm, idProvincia, idLocalidad);
        Sort sort = Sort.by("apellido").ascending().and(Sort.by("nombre").ascending());

        List<PacienteListadoDTO> pacientes = pacienteRepository.findAll(spec, sort).stream()
                .limit(500)
                .map(this::toListadoDTO)
                .sorted(Comparator
                        .comparing(PacienteListadoDTO::getApellido, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(PacienteListadoDTO::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());

        return PacienteBusquedaResponseDTO.builder()
                .total(pacientes.size())
                .pacientes(pacientes)
                .criteriosAplicados(describirCriteriosBusqueda(textoNorm, nombreProvincia, nombreLocalidad))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteBusquedaResponseDTO buscarPacientesEnUbicacion(
            String texto, Long idProvincia, Long idLocalidad) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaDeNegocioException("Indicá apellido, nombre o DNI para buscar.");
        }
        if (idProvincia == null || idLocalidad == null) {
            throw new ReglaDeNegocioException("La búsqueda requiere provincia y localidad del profesional.");
        }

        localidadRepository.findById(idLocalidad).ifPresent(loc -> {
            if (!loc.getProvincia().getIdProvincia().equals(idProvincia)) {
                throw new ReglaDeNegocioException("La localidad no pertenece a la provincia indicada.");
            }
        });

        String textoNorm = texto.trim();
        String nombreProvincia = provinciaRepository.findById(idProvincia)
                .map(Provincia::getNombre)
                .orElse("provincia");
        String nombreLocalidad = localidadRepository.findById(idLocalidad)
                .map(Localidad::getNombre)
                .orElse("localidad");

        Specification<Paciente> spec =
                PacienteSpecifications.busquedaEnUbicacion(textoNorm, idProvincia, idLocalidad);
        Sort sort = Sort.by("apellido").ascending().and(Sort.by("nombre").ascending());

        List<PacienteListadoDTO> pacientes = pacienteRepository.findAll(spec, sort).stream()
                .limit(500)
                .map(this::toListadoDTO)
                .collect(Collectors.toList());

        String criterios = textoNorm.matches("\\d+")
                ? "DNI: \"" + textoNorm + "\", " + nombreLocalidad + " (" + nombreProvincia + ")"
                : "Apellido/nombre: \"" + textoNorm + "\", " + nombreLocalidad + " (" + nombreProvincia + ")";

        return PacienteBusquedaResponseDTO.builder()
                .total(pacientes.size())
                .pacientes(pacientes)
                .criteriosAplicados(criterios)
                .build();
    }

    private String describirCriteriosBusqueda(String texto, String provincia, String localidad) {
        boolean tieneTexto = texto != null && !texto.isBlank();
        boolean tieneProvincia = provincia != null && !provincia.isBlank();
        boolean tieneLocalidad = localidad != null && !localidad.isBlank();

        if (tieneTexto && !tieneProvincia) {
            return "Apellido y/o nombre: \"" + texto + "\"";
        }
        if (tieneProvincia && tieneLocalidad && tieneTexto) {
            return "Provincia " + provincia + ", localidad " + localidad + ", apellido/nombre: \"" + texto + "\"";
        }
        if (tieneProvincia && tieneLocalidad) {
            return "Provincia " + provincia + ", localidad " + localidad;
        }
        if (tieneProvincia && tieneTexto) {
            return "Provincia " + provincia + ", apellido/nombre: \"" + texto + "\"";
        }
        if (tieneProvincia) {
            return "Provincia " + provincia;
        }
        return "Criterios de búsqueda";
    }

    private PacienteListadoDTO toListadoDTO(Paciente p) {
        String provincia = null;
        String localidad = null;
        if (p.getDireccion() != null && p.getDireccion().getLocalidad() != null) {
            localidad = p.getDireccion().getLocalidad().getNombre();
            if (p.getDireccion().getLocalidad().getProvincia() != null) {
                provincia = p.getDireccion().getLocalidad().getProvincia().getNombre();
            }
        }
        return PacienteListadoDTO.builder()
                .idUsuario(p.getIdUsuario())
                .apellido(p.getApellido())
                .nombre(p.getNombre())
                .dni(p.getDni())
                .nombreProvincia(provincia)
                .nombreLocalidad(localidad)
                .build();
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
        rechazarEdicionPerfilSiProfesional(id);
        Paciente paciente = pacienteRepository.findWithUbicacionById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado con ID: " + id));

        cuentaEntidadHelper.validarActualizacion(paciente.getIdUsuario(), dto.getEmail(), dto.getDni());

        Usuario usuario = paciente.getUsuario();
        paciente.setNombre(dto.getNombre());
        paciente.setApellido(dto.getApellido());
        paciente.setDni(dto.getDni());
        usuario.setEmail(dto.getEmail());
        paciente.setTelefono(dto.getTelefono());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setSexo(CuentaEntidadHelper.sexoAsString(dto.getSexo()));
        paciente.setNumeroAfiliado(dto.getNumeroAfiliado());

        if (dto.getEstadoActual() != null && callerIsAdministrador()) {
            Estado nuevoEstado = estadoRepository.findByNombre(dto.getEstadoActual())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Estado no v?lido: " + dto.getEstadoActual()));

            if (!usuario.getEstadoActual().equals(nuevoEstado)) {
                usuario.setEstadoActual(nuevoEstado);
                registrarCambioEstado(usuario, nuevoEstado);
            }
        }

        if (dto.getRolesIds() != null && callerIsAdministrador()) {
            usuario.setRoles(buscarRoles(dto.getRolesIds()));
        }

        if (dto.getIdLocalidad() != null || (dto.getDireccion() != null && !dto.getDireccion().isBlank())) {
            Localidad localidadObjetivo = dto.getIdLocalidad() != null
                    ? localidadRepository.findById(dto.getIdLocalidad())
                            .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada"))
                    : paciente.getDireccion().getLocalidad();

            if (dto.getDireccion() != null && !dto.getDireccion().isBlank()) {
                paciente.setDireccion(direccionService.obtenerOCrearPorTextoYLocalidad(dto.getDireccion(), localidadObjetivo));
            } else if (dto.getIdLocalidad() != null
                    && paciente.getDireccion() != null
                    && paciente.getDireccion().getLocalidad() != null
                    && !paciente.getDireccion().getLocalidad().getIdLocalidad().equals(localidadObjetivo.getIdLocalidad())) {
                throw new ReglaDeNegocioException(
                        "Si cambiás la localidad, enviá la dirección en texto para esa localidad.");
            }
        }

        paciente.setObraSocial(obraSocialRepository.findById(dto.getIdObraSocial())
                .orElseThrow(() -> new RecursoNoEncontradoException("Obra Social no encontrada")));

        usuarioRepository.save(usuario);
        return pacienteMapper.toResponseDTO(pacienteRepository.save(paciente));
    }

    @Override
    @Transactional 
    public void eliminarSoloPaciente(Long id) {
        Paciente paciente = pacienteRepository.findByUsuario_IdUsuario(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El paciente con ID " + id + " no fue encontrado."));
        Usuario usuario = paciente.getUsuario();
        tokenRepository.deleteByUsuario(usuario);
        refreshTokenRepository.deleteByUsuario(usuario);
        cambioEstadoRepository.deleteByUsuario_IdUsuario(id);
        try {
            pacienteRepository.delete(paciente);
            usuarioRepository.delete(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaDeNegocioException("No se puede eliminar el paciente porque tiene registros asociados.");
        }
    }

    // --- METODOS PRIVADOS DE APOYO ---

    private boolean callerIsAdministrador() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMINISTRADOR"::equals);
    }

    private Set<Rol> buscarRoles(Set<Long> rolesIds) {
        return rolesIds.stream()
                .map(id -> rolRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + id)))
                .collect(Collectors.toSet());
    }

    private void registrarHistorialTrasActivacion(Usuario usuario) {
        registrarCambioEstado(usuario, usuario.getEstadoActual());
    }

    private void registrarCambioEstado(Usuario usuario, Estado estado) {
        CambioEstado cambio = CambioEstado.builder()
                .usuario(usuario)
                .estado(estado)
                .fecha(LocalDateTime.now())
                .build();
        cambioEstadoRepository.save(cambio);
    }

    private Paciente obtenerPacientePorEmail(String email) {
        return pacienteRepository.findByUsuario_Email(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un paciente registrado con el email: " + email));
    }

    private void validarNoBloqueado(Paciente paciente) {
        if (EstadoUsuario.BLOQUEADO.equalsIgnoreCase(CuentaEntidadHelper.nombreEstado(paciente))) {
            throw new ReglaDeNegocioException(
                    "Tu cuenta está bloqueada. Contactá a la clínica o al administrador para rehabilitar el acceso.");
        }
    }

    private void enviarCorreoConfirmacionRegistro(Paciente paciente) {
        Usuario usuario = paciente.getUsuario();
        VerificationTokenService.DatosConfirmacion datos = verificationTokenService.crearTokenConfirmacion(usuario);
        emailService.enviarEmailConfirmacion(usuario, datos.token(), datos.codigo());
    }

    private void enviarCorreoActivacionPaciente(Paciente paciente) {
        Usuario usuario = paciente.getUsuario();
        String token = verificationTokenService.crearTokenActivacionPaciente(usuario);
        String link = String.format("%s/usuarios/api/auth/confirmar-activacion-paciente?token=%s", appUrl, token);
        emailService.enviarEmailActivacionPaciente(usuario, link);
    }

    private void enviarCorreoRecuperacionPassword(Paciente paciente) {
        Usuario usuario = paciente.getUsuario();
        String token = verificationTokenService.crearTokenRecuperacionPassword(usuario);
        String link = String.format("%s/usuarios/api/auth/confirmar-cambio-password?token=%s&tipo=paciente", appUrl, token);
        emailService.enviarEmailRecuperacionPassword(usuario, link, "paciente");
    }

    private Paciente resolverPacienteDesdeTokenActivacion(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Este enlace ya fue utilizado o no es válido."));
        if (vToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new ReglaDeNegocioException(
                    "El enlace de activación ha expirado (válido por 72 horas). Solicitá uno nuevo desde el login.");
        }
        Paciente paciente = pacienteRepository.findByUsuario_IdUsuario(vToken.getUsuario().getIdUsuario())
                .orElseThrow(() -> new ReglaDeNegocioException("El enlace no corresponde a un paciente."));
        if (!EstadoUsuario.SIN_CONTRASENA.equalsIgnoreCase(CuentaEntidadHelper.nombreEstado(paciente))) {
            throw new ReglaDeNegocioException("Esta cuenta no está pendiente de activación con contraseña.");
        }
        return pacienteRepository.findWithUbicacionById(paciente.getIdUsuario())
                .orElse(paciente);
    }

    private PacienteActivacionDatosDTO toActivacionDatosDTO(Paciente paciente) {
        PacienteActivacionDatosDTO.PacienteActivacionDatosDTOBuilder b = PacienteActivacionDatosDTO.builder()
                .nombre(paciente.getNombre())
                .apellido(paciente.getApellido())
                .dni(paciente.getDni())
                .email(CuentaEntidadHelper.emailDe(paciente))
                .telefono(paciente.getTelefono())
                .fechaNacimiento(paciente.getFechaNacimiento())
                .sexo(CuentaEntidadHelper.sexoAsEnum(paciente.getSexo()))
                .numeroAfiliado(paciente.getNumeroAfiliado());
        if (paciente.getObraSocial() != null) {
            b.nombreObraSocial(paciente.getObraSocial().getDescripcion());
        }
        if (paciente.getDireccion() != null) {
            b.direccion(paciente.getDireccion().getNombre());
            if (paciente.getDireccion().getLocalidad() != null) {
                b.nombreLocalidad(paciente.getDireccion().getLocalidad().getNombre());
                if (paciente.getDireccion().getLocalidad().getProvincia() != null) {
                    b.nombreProvincia(paciente.getDireccion().getLocalidad().getProvincia().getNombre());
                }
            }
        }
        return b.build();
    }

}