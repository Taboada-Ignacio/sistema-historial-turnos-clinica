package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.request.ProfesionalUpdateDTO;
import com.clinica.usuarios.dto.response.ProfesionalPresentacionDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.service.ProfesionalService;
import com.clinica.usuarios.web.AccountConfirmationRedirectHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Registra un nuevo profesional médico y guarda su foto de perfil.
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
    @PostMapping("/reenviar-confirmacion")
    public ResponseEntity<?> reenviarConfirmacion(@RequestParam("email") String email) {
        profesionalService.reenviarCorreoConfirmacion(email);
        return ResponseEntity.ok().body(
            Map.of("mensaje", "Si el correo existe y no está activado, se ha enviado un nuevo enlace de confirmación.")
        );
    }

    /**
     * Listado con todos los datos sensibles (interno): solo administrador.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping
    public ResponseEntity<List<ProfesionalResponseDTO>> obtenerTodos() {
        List<ProfesionalResponseDTO> profesionales = profesionalService.obtenerTodosLosProfesionales();
        return ResponseEntity.ok(profesionales);
    }

    /**
     * Catálogo público: nombre, apellido, especialidad y dirección compuesta.
     * No incluye usuarios con rol administrador ni profesionales que no estén ACTIVOS.
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_PACIENTE', 'ROLE_PROFESIONAL')")
    @GetMapping("/presentacion")
    public ResponseEntity<List<ProfesionalPresentacionDTO>> listarParaPresentacion() {
        return ResponseEntity.ok(profesionalService.listarParaPresentacion());
    }

    /**
     * Ficha pública de un profesional (mismas reglas de exclusión que {@link #listarParaPresentacion()}).
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_PACIENTE', 'ROLE_PROFESIONAL')")
    @GetMapping("/{id}/presentacion")
    public ResponseEntity<ProfesionalPresentacionDTO> obtenerParaPresentacion(@PathVariable Long id) {
        return ResponseEntity.ok(profesionalService.obtenerParaPresentacion(id));
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
     * Actualiza los datos del profesional y registra cambios de estado si corresponde.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or (hasAuthority('ROLE_PROFESIONAL') and @authorizationRules.esMismoUsuario(#id))")
    @PutMapping("/{id}")
    public ResponseEntity<ProfesionalResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody ProfesionalUpdateDTO dto) {
        return ResponseEntity.ok(profesionalService.actualizarProfesional(id, dto));
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
     * Otorga acceso indefinido al profesional (Cortesía o convenio).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PutMapping("/{id}/acceso-indefinido")
    public ResponseEntity<Void> otorgarAccesoIndefinido(@PathVariable Long id) {
        profesionalService.otorgarAccesoIndefinido(id);
        return ResponseEntity.ok().build();
    }
}