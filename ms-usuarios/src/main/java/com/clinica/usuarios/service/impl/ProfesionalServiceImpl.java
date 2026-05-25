package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.PacienteCargaProfesionalDTO;
import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.response.PacienteCargaProfesionalResponseDTO;
import com.clinica.usuarios.dto.request.ProfesionalUpdateDTO;
import com.clinica.usuarios.dto.request.RechazarProfesionalPendienteDTO;
import com.clinica.usuarios.dto.response.PersonaEnZonaBusquedaResponseDTO;
import com.clinica.usuarios.dto.response.PersonaEnZonaDetalleDTO;
import com.clinica.usuarios.dto.response.PersonaEnZonaListadoDTO;
import com.clinica.usuarios.dto.response.ProfesionalBusquedaResponseDTO;
import com.clinica.usuarios.dto.response.ProfesionalListadoDTO;
import com.clinica.usuarios.dto.response.ProfesionalPresentacionDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.repository.ProfesionalSpecifications;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.ProfesionalMapper;
import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.service.AccountActivationService;
import com.clinica.usuarios.service.DireccionService;
import com.clinica.usuarios.service.EmailService;
import com.clinica.usuarios.service.ProfesionalFotoStorageService;
import com.clinica.usuarios.service.PacienteService;
import com.clinica.usuarios.service.ProfesionalService;
import com.clinica.usuarios.service.VerificationTokenService;
import com.clinica.usuarios.service.support.UnicidadUsuarioValidator;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfesionalServiceImpl implements ProfesionalService {

    // --- REPOSITORIOS DE IDENTIDAD Y ACCESO ---
    private final ProfesionalRepository profesionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final LocalidadRepository localidadRepository;
    private final ProvinciaRepository provinciaRepository;
    private final EspecialidadRepository especialidadRepository;
    
    // --- REPOSITORIOS DE ESTADO Y SEGURIDAD ---
    private final EstadoRepository estadoRepository;
    private final CambioEstadoRepository cambioEstadoRepository;
    private final VerificationTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenService verificationTokenService;
    private final AccountActivationService accountActivationService;
    private final EmailService emailService;
    
    // --- REPOSITORIOS DE MEMBRESÍA ---
    private final MembresiaRepository membresiaRepository;
    private final CambioMembresiaRepository cambioMembresiaRepository;
    
    // --- UTILIDADES ---
    private final ProfesionalMapper profesionalMapper;
    private final PasswordEncoder passwordEncoder;
    private final DireccionService direccionService;
    private final ProfesionalFotoStorageService fotoStorage;
    private final PacienteService pacienteService;

    @Override
    @Transactional
    public PacienteCargaProfesionalResponseDTO cargarPacientePorProfesional(
            String emailProfesional, PacienteCargaProfesionalDTO dto) {
        validarProfesionalPuedeBuscar(emailProfesional);
        return pacienteService.cargarPacienteSinPassword(dto);
    }

    @Override
    @Transactional
    public ProfesionalResponseDTO registrarProfesional(ProfesionalRegistroDTO dto, MultipartFile foto) {
        // 1. Validaciones de Identidad
        UnicidadUsuarioValidator.validarAlta(usuarioRepository, dto.getEmail(), dto.getDni());

        // 2. Obtención de dependencias (Localidad, Especialidad, Roles)
        Set<Rol> rolesAsignados = buscarRoles(dto.getRolesIds());
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada"));
        Especialidad especialidad = especialidadRepository.findById(dto.getIdEspecialidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));

        // 3. Definir estado y membresía iniciales (Pendiente y Sin Verificar)
        Estado estadoPendiente = estadoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new ReglaDeNegocioException("Estado inicial PENDIENTE no configurado en BD"));
        
        Membresia membresiaSinVerificar = membresiaRepository.findByNombre("SIN_VERIFICAR")
                .orElseThrow(() -> new ReglaDeNegocioException("Membresía inicial SIN_VERIFICAR no configurada en BD"));

        // 4. Mapeo y Configuración Base
        Profesional profesional = profesionalMapper.toEntity(dto);
        profesional.setRoles(rolesAsignados);
        profesional.setDireccion(direccionService.obtenerOCrearPorTextoYLocalidad(dto.getDireccion(), localidad));
        profesional.setEspecialidad(especialidad);
        // Hasheamos el password antes de guardar
        profesional.setPassword(passwordEncoder.encode(dto.getPassword()));
        profesional.setEstadoActual(estadoPendiente);
        profesional.setMembresiaActual(membresiaSinVerificar);

        // 5. Guardado inicial en BD (foto después, para evitar archivos huérfanos si falla el alta)
        Profesional profesionalGuardado = profesionalRepository.save(profesional);

        if (foto != null && !foto.isEmpty()) {
            String rutaFoto = fotoStorage.guardar(foto);
            profesionalGuardado.setFotoPerfil(rutaFoto);
            profesionalGuardado = profesionalRepository.save(profesionalGuardado);
        }

        // 7. Auditoría de estado, membresía y generación de Token
        registrarHistorialEstado(profesionalGuardado, estadoPendiente);
        registrarCambioMembresia(profesionalGuardado, membresiaSinVerificar, LocalDateTime.now(), null);
        
        VerificationTokenService.DatosConfirmacion datos = verificationTokenService.crearTokenConfirmacion(profesionalGuardado);
        emailService.enviarEmailConfirmacion(profesionalGuardado, datos.token(), datos.codigo());

        return profesionalMapper.toResponseDTO(profesionalGuardado);
    }

    @Override
    @Transactional
    public void confirmarCuenta(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de confirmación inválido o inexistente"));
        accountActivationService.confirmarCuentaDesdeToken(vToken, this::registrarHistorialTrasActivacion);
    }

    @Override
    @Transactional
    public void confirmarCuentaConCodigo(String email, String codigo) {
        VerificationToken vToken = tokenRepository.findByUsuario_EmailAndCodigo(email, codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Código de confirmación inválido."));

        if (!(vToken.getUsuario() instanceof Profesional)) {
            throw new ReglaDeNegocioException("El correo no corresponde a un profesional registrado.");
        }

        accountActivationService.confirmarCuentaDesdeToken(vToken, this::registrarHistorialTrasActivacion);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfesionalResponseDTO obtenerProfesionalPorId(Long id) {
        Profesional profesional = profesionalRepository.findWithUbicacionById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado con ID: " + id));
        return profesionalMapper.toResponseDTO(profesional);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfesionalBusquedaResponseDTO buscarProfesionales(
            String texto, Long idEspecialidad, Long idProvincia, Long idLocalidad) {
        boolean tieneTexto = texto != null && !texto.isBlank();
        boolean tieneEspecialidad = idEspecialidad != null;
        boolean tieneProvincia = idProvincia != null;
        boolean tieneLocalidad = idLocalidad != null;

        if (!tieneTexto && !tieneEspecialidad && !tieneProvincia) {
            throw new ReglaDeNegocioException(
                    "Indicá al menos apellido/nombre, una especialidad o una provincia para buscar profesionales.");
        }
        if (tieneLocalidad && !tieneProvincia) {
            throw new ReglaDeNegocioException("Para filtrar por localidad debés seleccionar también la provincia.");
        }

        String nombreEspecialidad = null;
        if (tieneEspecialidad) {
            nombreEspecialidad = especialidadRepository.findById(idEspecialidad)
                    .map(Especialidad::getDescripcion)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada."));
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
                    .orElseThrow(() -> new RecursoNoEncontradoException("Provincia no encontrada."));
        }

        String textoNorm = tieneTexto ? texto.trim() : null;
        Specification<Profesional> spec = ProfesionalSpecifications.busquedaAdmin(
                textoNorm, idEspecialidad, idProvincia, idLocalidad);
        Sort sort = Sort.by("apellido").ascending().and(Sort.by("nombre").ascending());

        List<ProfesionalListadoDTO> profesionales = profesionalRepository.findAll(spec, sort).stream()
                .limit(500)
                .map(this::toListadoDTO)
                .sorted(Comparator
                        .comparing(ProfesionalListadoDTO::getApellido, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(ProfesionalListadoDTO::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());

        return ProfesionalBusquedaResponseDTO.builder()
                .total(profesionales.size())
                .profesionales(profesionales)
                .criteriosAplicados(describirCriteriosBusquedaProfesional(
                        textoNorm, nombreEspecialidad, nombreProvincia, nombreLocalidad))
                .build();
    }

    private String describirCriteriosBusquedaProfesional(
            String texto, String especialidad, String provincia, String localidad) {
        boolean tieneTexto = texto != null && !texto.isBlank();
        boolean tieneEspecialidad = especialidad != null && !especialidad.isBlank();
        boolean tieneProvincia = provincia != null && !provincia.isBlank();
        boolean tieneLocalidad = localidad != null && !localidad.isBlank();

        StringBuilder sb = new StringBuilder();
        if (tieneEspecialidad) {
            sb.append("Especialidad ").append(especialidad);
        }
        if (tieneProvincia) {
            if (sb.length() > 0) sb.append("; ");
            if (tieneLocalidad) {
                sb.append("Provincia ").append(provincia).append(", localidad ").append(localidad);
            } else {
                sb.append("Provincia ").append(provincia);
            }
        }
        if (tieneTexto) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("Apellido/nombre: \"").append(texto).append("\"");
        }
        return sb.length() > 0 ? sb.toString() : "Criterios de búsqueda";
    }

    private ProfesionalListadoDTO toListadoDTO(Profesional p) {
        return ProfesionalListadoDTO.builder()
                .idUsuario(p.getIdUsuario())
                .fotoPerfil(p.getFotoPerfil())
                .apellido(p.getApellido())
                .nombre(p.getNombre())
                .dni(p.getDni())
                .especialidad(p.getEspecialidad() != null ? p.getEspecialidad().getDescripcion() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfesionalResponseDTO obtenerProfesionalSesion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        if (!(usuario instanceof Profesional profesional)) {
            throw new ReglaDeNegocioException("La cuenta no corresponde a un profesional de la salud.");
        }
        return profesionalMapper.toResponseDTO(profesional);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaEnZonaBusquedaResponseDTO buscarPersonasEnMiUbicacion(String emailProfesional, String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaDeNegocioException("Indicá apellido, nombre o DNI para buscar.");
        }
        UbicacionProfesional zona = resolverUbicacionProfesionalLogueado(emailProfesional);
        return ejecutarBusquedaPersonasEnUbicacion(texto.trim(), zona);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaEnZonaBusquedaResponseDTO buscarPersonasEnUbicacionGeneral(
            String emailProfesional, String texto, Long idProvincia, Long idLocalidad) {
        validarProfesionalPuedeBuscar(emailProfesional);
        if (texto == null || texto.isBlank()) {
            throw new ReglaDeNegocioException("Indicá apellido, nombre o DNI para buscar.");
        }
        if (idProvincia == null || idLocalidad == null) {
            throw new ReglaDeNegocioException("Seleccioná provincia y localidad para la búsqueda.");
        }
        UbicacionProfesional zona = resolverUbicacionPorIds(idProvincia, idLocalidad);
        return ejecutarBusquedaPersonasEnUbicacion(texto.trim(), zona);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaEnZonaDetalleDTO obtenerPersonaEnUbicacion(
            String emailProfesional, Long idUsuario, Long idProvincia, Long idLocalidad) {
        UbicacionProfesional zona;
        if (idProvincia == null && idLocalidad == null) {
            zona = resolverUbicacionProfesionalLogueado(emailProfesional);
        } else {
            validarProfesionalPuedeBuscar(emailProfesional);
            if (idProvincia == null || idLocalidad == null) {
                throw new ReglaDeNegocioException("Indicá provincia y localidad para ver el detalle.");
            }
            zona = resolverUbicacionPorIds(idProvincia, idLocalidad);
        }
        return obtenerPersonaEnZona(idUsuario, zona);
    }

    private PersonaEnZonaBusquedaResponseDTO ejecutarBusquedaPersonasEnUbicacion(
            String textoNorm, UbicacionProfesional zona) {
        Specification<Usuario> spec = UsuarioSpecifications.busquedaPersonasEnUbicacion(
                textoNorm, zona.idProvincia(), zona.idLocalidad());
        Sort sort = Sort.by("apellido").ascending().and(Sort.by("nombre").ascending());

        List<PersonaEnZonaListadoDTO> personas = usuarioRepository.findAll(spec, sort).stream()
                .limit(500)
                .map(this::toPersonaEnZonaListado)
                .collect(Collectors.toList());

        String criterios = textoNorm.matches("\\d+")
                ? "DNI: \"" + textoNorm + "\", " + zona.nombreLocalidad() + " (" + zona.nombreProvincia() + ")"
                : "Apellido/nombre: \"" + textoNorm + "\", " + zona.nombreLocalidad() + " (" + zona.nombreProvincia() + ")";

        return PersonaEnZonaBusquedaResponseDTO.builder()
                .total(personas.size())
                .personas(personas)
                .criteriosAplicados(criterios)
                .build();
    }

    private PersonaEnZonaDetalleDTO obtenerPersonaEnZona(Long idUsuario, UbicacionProfesional zona) {
        Usuario persona = usuarioRepository.findWithUbicacionById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la persona en la zona indicada."));

        if (!(persona instanceof Paciente)
                && !(persona instanceof Profesional)
                && !(persona instanceof Administrador)) {
            throw new RecursoNoEncontradoException("No se encontró la persona en la zona indicada.");
        }

        if (!estaEnMismaZona(persona, zona.idProvincia(), zona.idLocalidad())) {
            throw new RecursoNoEncontradoException("No se encontró la persona en la zona indicada.");
        }

        if (estaBloqueadoParaBusquedaProfesional(persona)) {
            throw new RecursoNoEncontradoException("No se encontró la persona en la zona indicada.");
        }

        return toPersonaEnZonaDetalle(persona);
    }

    /** Pacientes y profesionales bloqueados no son visibles en búsqueda del portal profesional. */
    private static boolean estaBloqueadoParaBusquedaProfesional(Usuario persona) {
        if (!(persona instanceof Paciente) && !(persona instanceof Profesional)) {
            return false;
        }
        return persona.getEstadoActual() != null
                && "BLOQUEADO".equalsIgnoreCase(persona.getEstadoActual().getNombre());
    }

    private void validarProfesionalPuedeBuscar(String emailProfesional) {
        Usuario usuario = usuarioRepository.findByEmail(emailProfesional)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        if (!(usuario instanceof Profesional profesional)) {
            throw new ReglaDeNegocioException("La cuenta no corresponde a un profesional de la salud.");
        }
        Profesional conMembresia = profesionalRepository.findWithUbicacionById(profesional.getIdUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado."));
        if (conMembresia.getMembresiaActual() != null
                && "SIN_VERIFICAR".equalsIgnoreCase(conMembresia.getMembresiaActual().getNombre())) {
            throw new ReglaDeNegocioException(
                    "Tu cuenta está pendiente de verificación. No podés buscar pacientes hasta que un administrador habilite tu matrícula.");
        }
    }

    private UbicacionProfesional resolverUbicacionProfesionalLogueado(String emailProfesional) {
        validarProfesionalPuedeBuscar(emailProfesional);
        Usuario usuario = usuarioRepository.findByEmail(emailProfesional)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        Profesional profesional = (Profesional) usuario;

        Profesional conUbicacion = profesionalRepository.findWithUbicacionById(profesional.getIdUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado."));

        if (conUbicacion.getDireccion() == null || conUbicacion.getDireccion().getLocalidad() == null) {
            throw new ReglaDeNegocioException(
                    "Completá provincia y localidad en tu perfil profesional para buscar pacientes de tu zona.");
        }

        Long idLocalidad = conUbicacion.getDireccion().getLocalidad().getIdLocalidad();
        Long idProvincia = conUbicacion.getDireccion().getLocalidad().getProvincia().getIdProvincia();
        String nombreLocalidad = conUbicacion.getDireccion().getLocalidad().getNombre();
        String nombreProvincia = conUbicacion.getDireccion().getLocalidad().getProvincia().getNombre();

        return new UbicacionProfesional(idProvincia, idLocalidad, nombreProvincia, nombreLocalidad);
    }

    private UbicacionProfesional resolverUbicacionPorIds(Long idProvincia, Long idLocalidad) {
        Localidad localidad = localidadRepository.findById(idLocalidad)
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada."));
        if (!localidad.getProvincia().getIdProvincia().equals(idProvincia)) {
            throw new ReglaDeNegocioException("La localidad no pertenece a la provincia indicada.");
        }
        Provincia provincia = provinciaRepository.findById(idProvincia)
                .orElseThrow(() -> new RecursoNoEncontradoException("Provincia no encontrada."));
        return new UbicacionProfesional(
                idProvincia, idLocalidad, provincia.getNombre(), localidad.getNombre());
    }

    private boolean estaEnMismaZona(Usuario persona, Long idProvincia, Long idLocalidad) {
        if (persona.getDireccion() == null || persona.getDireccion().getLocalidad() == null) {
            return false;
        }
        Localidad loc = persona.getDireccion().getLocalidad();
        return loc.getIdLocalidad().equals(idLocalidad)
                && loc.getProvincia() != null
                && loc.getProvincia().getIdProvincia().equals(idProvincia);
    }

    private PersonaEnZonaListadoDTO toPersonaEnZonaListado(Usuario u) {
        String nombreProvincia = null;
        String nombreLocalidad = null;
        if (u.getDireccion() != null && u.getDireccion().getLocalidad() != null) {
            Localidad loc = u.getDireccion().getLocalidad();
            nombreLocalidad = loc.getNombre();
            if (loc.getProvincia() != null) {
                nombreProvincia = loc.getProvincia().getNombre();
            }
        }
        return PersonaEnZonaListadoDTO.builder()
                .idUsuario(u.getIdUsuario())
                .apellido(u.getApellido())
                .nombre(u.getNombre())
                .dni(u.getDni())
                .tipoCuenta(tipoCuentaDe(u))
                .nombreProvincia(nombreProvincia)
                .nombreLocalidad(nombreLocalidad)
                .build();
    }

    private PersonaEnZonaDetalleDTO toPersonaEnZonaDetalle(Usuario u) {
        String nombreProvincia = null;
        String nombreLocalidad = null;
        String direccion = null;
        if (u.getDireccion() != null) {
            direccion = u.getDireccion().getNombre();
            if (u.getDireccion().getLocalidad() != null) {
                Localidad loc = u.getDireccion().getLocalidad();
                nombreLocalidad = loc.getNombre();
                if (loc.getProvincia() != null) {
                    nombreProvincia = loc.getProvincia().getNombre();
                }
            }
        }
        return PersonaEnZonaDetalleDTO.builder()
                .idUsuario(u.getIdUsuario())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .dni(u.getDni())
                .telefono(u.getTelefono())
                .fechaNacimiento(u.getFechaNacimiento())
                .sexo(u.getSexo())
                .nombreLocalidad(nombreLocalidad)
                .nombreProvincia(nombreProvincia)
                .direccion(direccion)
                .tipoCuenta(tipoCuentaDe(u))
                .build();
    }

    private static String tipoCuentaDe(Usuario u) {
        if (u instanceof Paciente) {
            return "PACIENTE";
        }
        if (u instanceof Profesional) {
            return "PROFESIONAL";
        }
        if (u instanceof Administrador) {
            return "ADMINISTRADOR";
        }
        return "DESCONOCIDO";
    }

    private record UbicacionProfesional(
            Long idProvincia, Long idLocalidad, String nombreProvincia, String nombreLocalidad) {}

    @Override
    @Transactional(readOnly = true)
    public List<ProfesionalResponseDTO> obtenerTodosLosProfesionales() {
        return profesionalRepository.findAll().stream()
                .map(profesionalMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesionalResponseDTO> obtenerProfesionalesPorMembresiaNombre(String nombreMembresia) {
        if (nombreMembresia == null || nombreMembresia.isBlank()) {
            throw new ReglaDeNegocioException("El parámetro membresia no puede estar vacío.");
        }
        String normalized = nombreMembresia.trim().toUpperCase();
        membresiaRepository.findByNombre(normalized)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una membresía con nombre: " + normalized));
        return profesionalRepository.findByMembresiaActual_Nombre(normalized).stream()
                .map(profesionalMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesionalPresentacionDTO> listarParaPresentacion() {
        return profesionalRepository.findAllWithUbicacionAndEspecialidad().stream()
                .filter(this::incluirEnCatalogoPresentacion)
                .map(this::toPresentacionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProfesionalPresentacionDTO obtenerParaPresentacion(Long id, String emailSolicitante) {
        Profesional profesional = profesionalRepository.findWithUbicacionById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado con ID: " + id));
        boolean esPropio = emailSolicitante != null
                && profesional.getEmail() != null
                && emailSolicitante.equalsIgnoreCase(profesional.getEmail());
        if (!esPropio && !incluirEnCatalogoPresentacion(profesional)) {
            throw new RecursoNoEncontradoException("Profesional no encontrado con ID: " + id);
        }
        return toPresentacionDTO(profesional);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esFotoVisibleEnCatalogoPublico(String fileName) {
        if (fileName == null || fileName.isBlank() || fileName.contains("..") || fileName.contains("/")) {
            return false;
        }
        return profesionalRepository.findByFotoPerfilFileName(fileName.trim())
                .map(this::incluirEnCatalogoPresentacion)
                .orElse(false);
    }

    /**
     * Catálogo público: estado ACTIVO y sin rol de administrador (cuentas admin no se listan).
     */
    private boolean incluirEnCatalogoPresentacion(Profesional p) {
        if (p.getRoles().stream().anyMatch(r -> "ROLE_ADMINISTRADOR".equals(r.getDescripcion()))) {
            return false;
        }
        return p.getEstadoActual() != null
                && "ACTIVO".equalsIgnoreCase(p.getEstadoActual().getNombre());
    }

    private ProfesionalPresentacionDTO toPresentacionDTO(Profesional p) {
        return ProfesionalPresentacionDTO.builder()
                .idUsuario(p.getIdUsuario())
                .nombre(p.getNombre())
                .apellido(p.getApellido())
                .especialidad(p.getEspecialidad() != null ? p.getEspecialidad().getDescripcion() : null)
                .direccion(formatDireccionPresentacion(p))
                .fotoPerfil(p.getFotoPerfil())
                .build();
    }

    private String formatDireccionPresentacion(Usuario u) {
        List<String> partes = new ArrayList<>();
        if (u.getDireccion() != null && u.getDireccion().getNombre() != null && !u.getDireccion().getNombre().isBlank()) {
            partes.add(u.getDireccion().getNombre().trim());
        }
        if (u.getDireccion() != null && u.getDireccion().getLocalidad() != null
                && u.getDireccion().getLocalidad().getNombre() != null
                && !u.getDireccion().getLocalidad().getNombre().isBlank()) {
            partes.add(u.getDireccion().getLocalidad().getNombre().trim());
        }
        if (u.getDireccion() != null && u.getDireccion().getLocalidad() != null
                && u.getDireccion().getLocalidad().getProvincia() != null
                && u.getDireccion().getLocalidad().getProvincia().getNombre() != null
                && !u.getDireccion().getLocalidad().getProvincia().getNombre().isBlank()) {
            partes.add(u.getDireccion().getLocalidad().getProvincia().getNombre().trim());
        }
        return String.join(" - ", partes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesionalResponseDTO> obtenerProfesionalesConMembresiaInactiva() {
        Membresia membresiaInactiva = membresiaRepository.findByNombre("INACTIVA")
                .orElseThrow(() -> new RecursoNoEncontradoException("Membresía INACTIVA no encontrada"));
        
        return profesionalRepository.findByMembresiaActual(membresiaInactiva).stream()
                .map(profesionalMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProfesionalResponseDTO actualizarProfesional(Long id, ProfesionalUpdateDTO dto, MultipartFile foto) {
        Profesional profesional = profesionalRepository.findWithUbicacionById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado con ID: " + id));

        UnicidadUsuarioValidator.validarActualizacion(
                usuarioRepository, profesional.getIdUsuario(), dto.getEmail(), dto.getDni());
        validarMatriculaUnica(dto.getMatricula(), profesional.getIdUsuario());

        profesional.setNombre(dto.getNombre());
        profesional.setApellido(dto.getApellido());
        profesional.setDni(dto.getDni()); 
        profesional.setEmail(dto.getEmail()); 
        profesional.setTelefono(dto.getTelefono());
        profesional.setSexo(dto.getSexo());
        profesional.setMatricula(dto.getMatricula());

        if (dto.getIdEspecialidad() != null) {
            Especialidad especialidad = especialidadRepository.findById(dto.getIdEspecialidad())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));
            profesional.setEspecialidad(especialidad);
        }

        if (dto.getIdLocalidad() != null || (dto.getDireccion() != null && !dto.getDireccion().isBlank())) {
            Localidad localidadObjetivo = dto.getIdLocalidad() != null
                    ? localidadRepository.findById(dto.getIdLocalidad())
                            .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada"))
                    : profesional.getDireccion().getLocalidad();
            if (dto.getDireccion() != null && !dto.getDireccion().isBlank()) {
                profesional.setDireccion(direccionService.obtenerOCrearPorTextoYLocalidad(dto.getDireccion(), localidadObjetivo));
            } else if (dto.getIdLocalidad() != null
                    && profesional.getDireccion() != null
                    && profesional.getDireccion().getLocalidad() != null
                    && !profesional.getDireccion().getLocalidad().getIdLocalidad().equals(localidadObjetivo.getIdLocalidad())) {
                throw new ReglaDeNegocioException(
                        "Si cambiás la localidad, enviá la dirección en texto para esa localidad.");
            }
        }

        // Actualización de estado: solo administrador
        if (dto.getEstadoActual() != null && callerIsAdministrador()) {
            Estado nuevoEstado = estadoRepository.findByNombre(dto.getEstadoActual())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Estado solicitado no válido"));
            
            if (!profesional.getEstadoActual().equals(nuevoEstado)) {
                profesional.setEstadoActual(nuevoEstado);
                registrarHistorialEstado(profesional, nuevoEstado);
            }
        }

        if (dto.getRolesIds() != null && callerIsAdministrador()) {
            profesional.setRoles(buscarRoles(dto.getRolesIds()));
        }

        if (foto != null && !foto.isEmpty()) {
            String anterior = profesional.getFotoPerfil();
            String nuevaRuta = fotoStorage.guardar(foto);
            profesional.setFotoPerfil(nuevaRuta);
            profesional = profesionalRepository.save(profesional);
            fotoStorage.borrarSiExiste(anterior);
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

    @Override
    @Transactional
    public void rechazarYBorrarProfesionalPendiente(
            Long idProfesional, RechazarProfesionalPendienteDTO dto, String emailAdministrador) {
        Usuario admin = usuarioRepository.findByEmail(emailAdministrador)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado."));
        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new ReglaDeNegocioException("Contraseña incorrecta.");
        }

        Profesional profesional = buscarProfesional(idProfesional);
        if (profesional.getMembresiaActual() == null
                || !"SIN_VERIFICAR".equalsIgnoreCase(profesional.getMembresiaActual().getNombre())) {
            throw new ReglaDeNegocioException(
                    "Solo se puede rechazar y borrar profesionales con membresía SIN_VERIFICAR pendiente de revisión.");
        }

        String motivo = dto.getMotivo().trim();
        emailService.enviarEmailRechazoProfesionalPendiente(profesional, motivo);
        eliminarProfesionalPendienteEnCascada(profesional);
    }

    private void eliminarProfesionalPendienteEnCascada(Profesional profesional) {
        Long id = profesional.getIdUsuario();
        fotoStorage.borrarSiExiste(profesional.getFotoPerfil());
        tokenRepository.deleteByUsuario(profesional);
        refreshTokenRepository.deleteByUsuario(profesional);
        cambioMembresiaRepository.deleteByProfesional_IdUsuario(id);
        cambioEstadoRepository.deleteByUsuario_IdUsuario(id);
        try {
            profesionalRepository.delete(profesional);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaDeNegocioException(
                    "No se puede eliminar el profesional porque tiene registros asociados en el sistema.");
        }
    }

    // --- MÉTODOS DE NEGOCIO PARA MEMBRESÍAS ---

    @Override
    @Transactional
    public void verificarMatricula(Long idProfesional) {
        Profesional profesional = buscarProfesional(idProfesional);
        
        Membresia membresiaInactiva = membresiaRepository.findByNombre("INACTIVA")
                .orElseThrow(() -> new ReglaDeNegocioException("Membresía INACTIVA no encontrada"));

        // Solo enviar email si el cambio de membresía es real
        if (!profesional.getMembresiaActual().equals(membresiaInactiva)) {
            actualizarMembresia(profesional, membresiaInactiva, null);
            // Enviar email de aprobación
            emailService.enviarEmailAprobacion(profesional);
        }
    }

    @Override
    @Transactional
    public void otorgarAccesoIndefinido(Long idProfesional) {
        Profesional profesional = buscarProfesional(idProfesional);
        
        Membresia membresiaIndefinida = membresiaRepository.findByNombre("ACCESO_INDEFINIDO")
                .orElseThrow(() -> new ReglaDeNegocioException("Membresía ACCESO_INDEFINIDO no encontrada"));

        actualizarMembresia(profesional, membresiaIndefinida, null);
    }

    // ========================================================================
    // --- MÉTODOS PRIVADOS AUXILIARES ---
    // ========================================================================

    private void actualizarMembresia(Profesional profesional, Membresia nuevaMembresia, LocalDateTime vencimiento) {
        if (!profesional.getMembresiaActual().equals(nuevaMembresia)) {
            profesional.setMembresiaActual(nuevaMembresia);
            profesionalRepository.save(profesional);
            registrarCambioMembresia(profesional, nuevaMembresia, LocalDateTime.now(), vencimiento);
        }
    }

    private void registrarCambioMembresia(Profesional profesional, Membresia membresia, LocalDateTime inicio, LocalDateTime vencimiento) {
        CambioMembresia cambio = CambioMembresia.builder()
                .profesional(profesional)
                .membresia(membresia)
                .fechaInicio(inicio)
                .fechaVencimiento(vencimiento)
                .build();
        cambioMembresiaRepository.save(cambio);
    }

    private void registrarHistorialEstado(Usuario usuario, Estado estado) {
        CambioEstado historial = CambioEstado.builder()
                .usuario(usuario)
                .estado(estado)
                .fecha(LocalDateTime.now())
                .build();
        cambioEstadoRepository.save(historial);
    }

    private void registrarHistorialTrasActivacion(Usuario usuario) {
        registrarHistorialEstado(usuario, usuario.getEstadoActual());
    }

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

    private Profesional buscarProfesional(Long id) {
        return profesionalRepository.findWithUbicacionById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado con ID: " + id));
    }

    private void validarMatriculaUnica(String matricula, Long idUsuario) {
        if (matricula == null || matricula.isBlank()) {
            return;
        }
        profesionalRepository.findByMatricula(matricula.trim())
                .filter(p -> !p.getIdUsuario().equals(idUsuario))
                .ifPresent(p -> {
                    throw new ReglaDeNegocioException(
                            "La matrícula ya está registrada por otro profesional.");
                });
    }

    @Override
    @Transactional
    public void reenviarCorreoConfirmacion(String email) {
        // 1. Buscar al usuario por su email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró un usuario registrado con el email: " + email));

        // 2. Validar que el usuario no esté ya ACTIVO
        if ("ACTIVO".equalsIgnoreCase(usuario.getEstadoActual().getNombre())) {
            throw new ReglaDeNegocioException("La cuenta ya se encuentra activa. No es necesario reenviar el correo.");
        }

        // 3. (Opcional pero recomendado) Eliminar tokens anteriores para ese usuario 
        // para que no se acumulen en la base de datos si pide el reenvío muchas veces.
        // Si no tenés este método en tokenRepository, podés crearlo: void deleteByUsuario(Usuario usuario);
        if (!(usuario instanceof Profesional profesional)) {
            throw new ReglaDeNegocioException("El correo no corresponde a un profesional registrado.");
        }

        VerificationTokenService.DatosConfirmacion datos = verificationTokenService.crearTokenConfirmacion(profesional);
        emailService.enviarEmailConfirmacion(profesional, datos.token(), datos.codigo());
    }
}