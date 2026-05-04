package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.request.ProfesionalUpdateDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.ProfesionalMapper;
import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.service.EmailService;
import com.clinica.usuarios.service.ProfesionalService;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
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
    private final EspecialidadRepository especialidadRepository;
    
    // --- REPOSITORIOS DE ESTADO Y SEGURIDAD ---
    private final EstadoRepository estadoRepository;
    private final CambioEstadoRepository cambioEstadoRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    
    // --- REPOSITORIOS DE MEMBRESÍA ---
    private final MembresiaRepository membresiaRepository;
    private final CambioMembresiaRepository cambioMembresiaRepository;
    
    // --- UTILIDADES ---
    private final ProfesionalMapper profesionalMapper;
    private final PasswordEncoder passwordEncoder;

    // Directorio local para guardar las imágenes
    private final String DIRECTORIO_FOTOS = "fotosPerfilProfesionales";

    @Override
    @Transactional
    public ProfesionalResponseDTO registrarProfesional(ProfesionalRegistroDTO dto, MultipartFile foto) {
        // 1. Validaciones de Identidad
        validarUnicidadProfesional(dto.getEmail(), dto.getDni());

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
        profesional.setLocalidad(localidad);
        profesional.setEspecialidad(especialidad);
        // Hasheamos el password antes de guardar
        profesional.setPassword(passwordEncoder.encode(dto.getPassword()));
        profesional.setEstadoActual(estadoPendiente);
        profesional.setMembresiaActual(membresiaSinVerificar);

        // 5. Manejo de la foto de perfil
        if (foto != null && !foto.isEmpty()) {
            String rutaFoto = guardarFotoLocalmente(foto);
            profesional.setFotoPerfil(rutaFoto);
        }

        // 6. Guardado inicial en BD
        Profesional profesionalGuardado = profesionalRepository.save(profesional);

        // 7. Auditoría de estado, membresía y generación de Token
        registrarHistorialEstado(profesionalGuardado, estadoPendiente);
        registrarCambioMembresia(profesionalGuardado, membresiaSinVerificar, LocalDateTime.now(), null);
        
        String token = crearTokenVerificacion(profesionalGuardado);

        // 8. Envío de Email de confirmación
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
        Profesional profesional = buscarProfesional(id);
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
        Profesional profesional = buscarProfesional(id);

        profesional.setNombre(dto.getNombre());
        profesional.setApellido(dto.getApellido());
        profesional.setDni(dto.getDni()); 
        profesional.setEmail(dto.getEmail()); 
        profesional.setTelefono(dto.getTelefono());
        profesional.setMatricula(dto.getMatricula());

        // Actualización de estado si viene en el DTO
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

    // --- MÉTODOS DE NEGOCIO PARA MEMBRESÍAS ---

    @Override
    @Transactional
    public void verificarMatricula(Long idProfesional) {
        Profesional profesional = buscarProfesional(idProfesional);
        
        Membresia membresiaInactiva = membresiaRepository.findByNombre("INACTIVA")
                .orElseThrow(() -> new ReglaDeNegocioException("Membresía INACTIVA no encontrada"));

        actualizarMembresia(profesional, membresiaInactiva, null);
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

    private String crearTokenVerificacion(Usuario usuario) {
        String tokenString = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.builder()
                .token(tokenString)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusHours(48)) 
                .build();
        tokenRepository.save(token);
        return tokenString; 
    }

    private void validarUnicidadProfesional(String email, Integer dni) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ReglaDeNegocioException("El email ya se encuentra en uso.");
        }
        if (usuarioRepository.findByDni(dni).isPresent()) {
            throw new ReglaDeNegocioException("El DNI ya se encuentra registrado.");
        }
    }

    private Set<Rol> buscarRoles(Set<Long> rolesIds) {
        return rolesIds.stream()
                .map(id -> rolRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + id)))
                .collect(Collectors.toSet());
    }

    private Profesional buscarProfesional(Long id) {
        return profesionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado con ID: " + id));
    }

    private String guardarFotoLocalmente(MultipartFile foto) {
        try {
            // Validamos explícitamente que sea .webp
            if (!foto.getOriginalFilename().toLowerCase().endsWith(".webp")) {
                throw new ReglaDeNegocioException("La foto de perfil debe ser estrictamente en formato .webp");
            }

            // Generamos un nombre único
            String nombreArchivo = UUID.randomUUID().toString() + ".webp";
            Path rutaDirectorio = Paths.get(DIRECTORIO_FOTOS);
            
            // Creamos el directorio si no existe (la primera vez que se ejecute)
            if (!Files.exists(rutaDirectorio)) {
                Files.createDirectories(rutaDirectorio);
            }

            // Guardamos el archivo
            Path rutaArchivo = rutaDirectorio.resolve(nombreArchivo);
            Files.copy(foto.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

            // Devolvemos la ruta que usará el frontend para consultar la imagen
            return "/" + DIRECTORIO_FOTOS + "/" + nombreArchivo;

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la foto de perfil en el disco local", e);
        }
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
        // tokenRepository.deleteByUsuario(usuario); 

        // 4. Generar un nuevo token usando tu método privado existente (que le da 48hs de validez)
        String nuevoToken = crearTokenVerificacion(usuario);

        // 5. Reenviar el correo usando tu servicio de email (Hacemos un cast a Profesional si tu emailService lo requiere específicamente, 
        // aunque asumo que recibe la clase padre Usuario o que el repositorio de usuario te devuelve la instancia correcta)
        if (usuario instanceof Profesional) {
             emailService.enviarEmailConfirmacion((Profesional) usuario, nuevoToken);
        } else {
             // Por si en un futuro usas este mismo método para administradores u otros roles
             emailService.enviarEmailConfirmacion(usuario, nuevoToken); 
        }
    }
}