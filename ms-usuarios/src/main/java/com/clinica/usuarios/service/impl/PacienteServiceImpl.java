package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.request.PacienteUpdateDTO;
import com.clinica.usuarios.dto.response.PacienteBusquedaResponseDTO;
import com.clinica.usuarios.dto.response.PacienteListadoDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
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
import com.clinica.usuarios.service.support.UnicidadUsuarioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
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

    @Override
    @Transactional
    public PacienteResponseDTO registrarPaciente(PacienteRegistroDTO dto) {
        
        // 1. Validaciones de Identidad
        UnicidadUsuarioValidator.validarAlta(usuarioRepository, dto.getEmail(), dto.getDni());

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
        paciente.setDireccion(direccionService.obtenerOCrearPorTextoYLocalidad(dto.getDireccion(), localidad));
        paciente.setObraSocial(obraSocial);
        paciente.setPassword(passwordEncoder.encode(dto.getPassword()));
        paciente.setEstadoActual(estadoPendiente); // Seteamos el objeto Estado
        
        // 5. Guardado del Paciente
        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        // 6. Registrar Auditor?a de Estado
        registrarCambioEstado(pacienteGuardado, estadoPendiente);

        // 7. Generar token/código y enviar correo
        VerificationTokenService.DatosConfirmacion datos = verificationTokenService.crearTokenConfirmacion(pacienteGuardado);
        emailService.enviarEmailConfirmacion(pacienteGuardado, datos.token(), datos.codigo());

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

        if (!(vToken.getUsuario() instanceof Paciente)) {
            throw new ReglaDeNegocioException("El correo no corresponde a un paciente registrado.");
        }

        accountActivationService.confirmarCuentaDesdeToken(vToken, this::registrarHistorialTrasActivacion);
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

        VerificationTokenService.DatosConfirmacion datos = verificationTokenService.crearTokenConfirmacion(usuario);
        emailService.enviarEmailConfirmacion(usuario, datos.token(), datos.codigo());
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
        Paciente paciente = pacienteRepository.findWithUbicacionById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado con ID: " + id));

        UnicidadUsuarioValidator.validarActualizacion(
                usuarioRepository, paciente.getIdUsuario(), dto.getEmail(), dto.getDni());

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

        return pacienteMapper.toResponseDTO(pacienteRepository.save(paciente));
    }

    @Override
    @Transactional 
    public void eliminarSoloPaciente(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El paciente con ID " + id + " no fue encontrado."));
        tokenRepository.deleteByUsuario(paciente);
        refreshTokenRepository.deleteByUsuario(paciente);
        cambioEstadoRepository.deleteByUsuario_IdUsuario(id);
        try {
            pacienteRepository.delete(paciente);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaDeNegocioException("No se puede eliminar el paciente porque tiene registros asociados.");
        }
    }

    // --- METODOS PRIVADOS DE APOYO ---

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

}