package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.ConfirmarCodigoDTO;
import com.clinica.usuarios.dto.request.PacienteCargaProfesionalDTO;
import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.response.PacienteCargaProfesionalResponseDTO;
import com.clinica.usuarios.dto.request.ProfesionalUpdateDTO;
import com.clinica.usuarios.dto.request.RechazarProfesionalPendienteDTO;
import com.clinica.usuarios.dto.response.PersonaEnZonaBusquedaResponseDTO;
import com.clinica.usuarios.dto.response.PersonaEnZonaDetalleDTO;
import com.clinica.usuarios.dto.response.ProfesionalBusquedaResponseDTO;
import com.clinica.usuarios.dto.response.ProfesionalPresentacionDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.service.ProfesionalService;
import com.clinica.usuarios.web.AccountConfirmationRedirectHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profesionales")
@RequiredArgsConstructor
public class ProfesionalController {

    private final ProfesionalService profesionalService;
    private final AccountConfirmationRedirectHelper confirmRedirect;

    /**
     * Registra un nuevo profesional de la salud y guarda su foto de perfil.
     * Se asigna el estado 'PENDIENTE', membresía 'SIN_VERIFICAR' y se envía el correo.
     */
    @PostMapping(value = "/registro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfesionalResponseDTO> registrar(
            @RequestPart("datos") @Valid ProfesionalRegistroDTO dto,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {
        
        ProfesionalResponseDTO nuevoProfesional = profesionalService.registrarProfesional(dto, foto);
        return new ResponseEntity<>(nuevoProfesional, HttpStatus.CREATED);
    }

    /**
     * Endpoint público para la confirmación de identidad vía email.
     * Redirecciona a la página de aprobación pendiente después de confirmar.
     */
    @GetMapping("/confirmar")
    public RedirectView confirmar(@RequestParam("token") String token) {
        try {
            profesionalService.confirmarCuenta(token);
            return confirmRedirect.exitoProfesional();
        } catch (Exception e) {
            return confirmRedirect.errorConfirmacion("profesional", e);
        }
    }

    /**
     * Reenvía el correo de confirmación si el token anterior expiró o no llegó.
     */
    @PostMapping("/confirmar-codigo")
    public ResponseEntity<?> confirmarConCodigo(@Valid @RequestBody ConfirmarCodigoDTO dto) {
        profesionalService.confirmarCuentaConCodigo(dto.getEmail(), dto.getCodigo());
        return ResponseEntity.ok(Map.of("mensaje", "Cuenta confirmada correctamente."));
    }

    @PostMapping("/reenviar-confirmacion")
    public ResponseEntity<?> reenviarConfirmacion(@RequestParam("email") String email) {
        profesionalService.reenviarCorreoConfirmacion(email);
        return ResponseEntity.ok().body(
            Map.of("mensaje", "Si el correo existe y no está activado, se ha enviado un nuevo enlace de confirmación.")
        );
    }

    /**
     * Listado con todos los datos sensibles (interno): solo administrador.
     * Query opcional {@code ?membresia=<NOMBRE>} filtra por membresía actual (nombre debe existir en catálogo, p. ej. {@code SIN_VERIFICAR}, {@code INACTIVA}).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping
    public ResponseEntity<List<ProfesionalResponseDTO>> obtenerTodos(
            @RequestParam(required = false) String membresia) {
        List<ProfesionalResponseDTO> profesionales =
                (membresia != null && !membresia.isBlank())
                        ? profesionalService.obtenerProfesionalesPorMembresiaNombre(membresia)
                        : profesionalService.obtenerTodosLosProfesionales();
        return ResponseEntity.ok(profesionales);
    }

    /**
     * Búsqueda de profesionales para administración (criterios independientes y combinables).
     * Filtros: {@code q}, {@code idEspecialidad}, {@code idProvincia}, {@code idLocalidad} (requiere provincia).
     * Al menos {@code q}, {@code idEspecialidad} o {@code idProvincia} es obligatorio.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping("/buscar")
    public ResponseEntity<ProfesionalBusquedaResponseDTO> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long idEspecialidad,
            @RequestParam(required = false) Long idProvincia,
            @RequestParam(required = false) Long idLocalidad) {
        return ResponseEntity.ok(
                profesionalService.buscarProfesionales(q, idEspecialidad, idProvincia, idLocalidad));
    }

    /**
     * Catálogo público: nombre, apellido, especialidad y dirección compuesta.
     * Sin JWT. No incluye administradores ni profesionales que no estén en estado ACTIVO.
     */
    @GetMapping("/presentacion")
    public ResponseEntity<List<ProfesionalPresentacionDTO>> listarParaPresentacion() {
        return ResponseEntity.ok(profesionalService.listarParaPresentacion());
    }

    /**
     * Ficha pública de un profesional (mismas reglas que {@link #listarParaPresentacion()}).
     * Sin JWT. Usuarios autenticados pueden ver su propia ficha aunque no estén ACTIVOS.
     */
    @GetMapping("/{id}/presentacion")
    public ResponseEntity<ProfesionalPresentacionDTO> obtenerParaPresentacion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        String email = principal != null ? principal.getUsername() : null;
        return ResponseEntity.ok(profesionalService.obtenerParaPresentacion(id, email));
    }

    /**
     * Perfil del profesional autenticado (portal staff).
     */
    @PreAuthorize("hasAuthority('ROLE_PROFESIONAL')")
    @GetMapping("/me")
    public ResponseEntity<ProfesionalResponseDTO> obtenerSesion(
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(profesionalService.obtenerProfesionalSesion(principal.getUsername()));
    }

    /**
     * Carga un paciente con usuario (estado SIN_CONTRASENA) y envía correo de activación (72 h).
     */
    @PreAuthorize("hasAuthority('ROLE_PROFESIONAL')")
    @PostMapping("/me/pacientes")
    public ResponseEntity<PacienteCargaProfesionalResponseDTO> cargarPaciente(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody PacienteCargaProfesionalDTO dto) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profesionalService.cargarPacientePorProfesional(principal.getUsername(), dto));
    }

    /**
     * Búsqueda de pacientes, profesionales y administradores en la misma provincia y localidad
     * del profesional autenticado. Parámetro {@code q}: apellido, nombre o DNI.
     */
    @PreAuthorize("hasAuthority('ROLE_PROFESIONAL')")
    @GetMapping("/me/pacientes/buscar")
    public ResponseEntity<PersonaEnZonaBusquedaResponseDTO> buscarPersonasEnMiUbicacion(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(name = "q") String q) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(
                profesionalService.buscarPersonasEnMiUbicacion(principal.getUsername(), q));
    }

    /**
     * Búsqueda general: pacientes, profesionales y administradores en la provincia y localidad elegidas.
     * Parámetros obligatorios: {@code q}, {@code idProvincia}, {@code idLocalidad}.
     */
    @PreAuthorize("hasAuthority('ROLE_PROFESIONAL')")
    @GetMapping("/me/pacientes/buscar-general")
    public ResponseEntity<PersonaEnZonaBusquedaResponseDTO> buscarPersonasEnUbicacionGeneral(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(name = "q") String q,
            @RequestParam(name = "idProvincia") Long idProvincia,
            @RequestParam(name = "idLocalidad") Long idLocalidad) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(profesionalService.buscarPersonasEnUbicacionGeneral(
                principal.getUsername(), q, idProvincia, idLocalidad));
    }

    /**
     * Detalle de una persona en una zona. Sin parámetros de ubicación usa la ciudad del profesional;
     * con {@code idProvincia} e {@code idLocalidad} valida contra esa zona (búsqueda general).
     */
    @PreAuthorize("hasAuthority('ROLE_PROFESIONAL')")
    @GetMapping("/me/pacientes/{id}")
    public ResponseEntity<PersonaEnZonaDetalleDTO> obtenerPersonaEnUbicacion(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @RequestParam(name = "idProvincia", required = false) Long idProvincia,
            @RequestParam(name = "idLocalidad", required = false) Long idLocalidad) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(profesionalService.obtenerPersonaEnUbicacion(
                principal.getUsername(), id, idProvincia, idLocalidad));
    }

    /**
     * Detalle completo por ID: administrador o el propio profesional.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or (hasAuthority('ROLE_PROFESIONAL') and @authorizationRules.esMismoUsuario(#id))")
    @GetMapping("/{id}")
    public ResponseEntity<ProfesionalResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(profesionalService.obtenerProfesionalPorId(id));
    }

    /**
     * Obtiene todos los profesionales con membresía INACTIVA (aprobados por administración).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping("/membresia/inactiva")
    public ResponseEntity<List<ProfesionalResponseDTO>> obtenerProfesionalesConMembresiaInactiva() {
        List<ProfesionalResponseDTO> profesionales = profesionalService.obtenerProfesionalesConMembresiaInactiva();
        return ResponseEntity.ok(profesionales);
    }

    /**
     * Actualiza los datos del profesional y registra cambios de estado si corresponde (JSON).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or (hasAuthority('ROLE_PROFESIONAL') and @authorizationRules.esMismoUsuario(#id))")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfesionalResponseDTO> actualizarJson(
            @PathVariable Long id,
            @Valid @RequestBody ProfesionalUpdateDTO dto) {
        return ResponseEntity.ok(profesionalService.actualizarProfesional(id, dto, null));
    }

    /**
     * Igual que {@link #actualizarJson} pero permite adjuntar una nueva foto de perfil (.webp).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or (hasAuthority('ROLE_PROFESIONAL') and @authorizationRules.esMismoUsuario(#id))")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfesionalResponseDTO> actualizarConFoto(
            @PathVariable Long id,
            @RequestPart("datos") @Valid ProfesionalUpdateDTO dto,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {
        return ResponseEntity.ok(profesionalService.actualizarProfesional(id, dto, foto));
    }

    /**
     * Elimina el registro del profesional (Borrado físico).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        profesionalService.eliminarSoloProfesional(id);
        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINTS DE GESTIÓN DE MEMBRESÍA ---

    /**
     * Verifica la matrícula del profesional y cambia su membresía a INACTIVA.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PutMapping("/{id}/verificar-matricula")
    public ResponseEntity<Void> verificarMatricula(@PathVariable Long id) {
        profesionalService.verificarMatricula(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Rechaza un profesional pendiente (SIN_VERIFICAR): valida contraseña del admin, notifica por email y borra el registro.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping("/{id}/rechazar-pendiente")
    public ResponseEntity<Map<String, String>> rechazarPendiente(
            @PathVariable Long id,
            @Valid @RequestBody RechazarProfesionalPendienteDTO dto,
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        profesionalService.rechazarYBorrarProfesionalPendiente(id, dto, principal.getUsername());
        return ResponseEntity.ok(Map.of(
                "mensaje", "El profesional fue notificado y su solicitud fue eliminada del sistema."));
    }

    /**
     * Otorga acceso indefinido al profesional (Cortesía o convenio).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PutMapping("/{id}/acceso-indefinido")
    public ResponseEntity<Void> otorgarAccesoIndefinido(@PathVariable Long id) {
        profesionalService.otorgarAccesoIndefinido(id);
        return ResponseEntity.ok().build();
    }
}