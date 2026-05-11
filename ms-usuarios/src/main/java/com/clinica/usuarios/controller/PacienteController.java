package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.request.PacienteUpdateDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.clinica.usuarios.web.AccountConfirmationRedirectHelper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;
    private final AccountConfirmationRedirectHelper confirmRedirect;

    /**
     * Registra un nuevo paciente.
     * El estado inicial será 'PENDIENTE' y se disparará el correo.
     */
    @PostMapping("/registro")
    public ResponseEntity<PacienteResponseDTO> registrar(@Valid @RequestBody PacienteRegistroDTO dto) {
        PacienteResponseDTO nuevoPaciente = pacienteService.registrarPaciente(dto);
        return new ResponseEntity<>(nuevoPaciente, HttpStatus.CREATED);
    }

    /**
     * Confirmación vía enlace del correo: activa la cuenta y redirige a la SPA (registro exitoso → dashboard).
     */
    @GetMapping("/confirmar")
    public RedirectView confirmarCuenta(@RequestParam("token") String token) {
        try {
            pacienteService.confirmarCuenta(token);
            return confirmRedirect.exitoPaciente();
        } catch (Exception e) {
            return confirmRedirect.errorConfirmacion("paciente", e);
        }
    }

    /**
     * Reenvía el correo de confirmación (pantalla “verificá tu correo”).
     */
    @PostMapping("/reenviar-confirmacion")
    public ResponseEntity<?> reenviarConfirmacion(@RequestParam("email") String email) {
        pacienteService.reenviarCorreoConfirmacion(email);
        return ResponseEntity.ok().body(
            Map.of("mensaje", "Si el correo existe y no está activado, se ha enviado un nuevo enlace de confirmación.")
        );
    }

    /**
     * Obtiene un paciente por su ID.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or (hasAuthority('ROLE_PACIENTE') and @authorizationRules.esMismoUsuario(#id))")
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerPacientePorId(id)); 
    }

    /**
     * Obtiene el listado completo de pacientes.
     * (Útil para el panel de administración de tu clínica).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> obtenerTodos() {
        List<PacienteResponseDTO> pacientes = pacienteService.obtenerTodosLosPacientes();
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Actualiza los datos de un paciente.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or (hasAuthority('ROLE_PACIENTE') and @authorizationRules.esMismoUsuario(#id))")
    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody PacienteUpdateDTO dto) {
        return ResponseEntity.ok(pacienteService.actualizarPaciente(id, dto));
    }

    /**
     * Elimina físicamente a un paciente si no tiene turnos asociados.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminarSoloPaciente(id);
        return ResponseEntity.noContent().build();
    }
}