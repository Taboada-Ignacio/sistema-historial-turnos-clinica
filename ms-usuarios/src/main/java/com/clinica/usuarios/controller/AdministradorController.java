package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.request.ConfirmarCodigoDTO;
import com.clinica.usuarios.dto.request.AdministradorUpdateDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;
import com.clinica.usuarios.service.AdministradorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.clinica.usuarios.web.AccountConfirmationRedirectHelper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/administradores")
@RequiredArgsConstructor
public class AdministradorController {

    private final AdministradorService administradorService;
    private final AccountConfirmationRedirectHelper confirmRedirect;

    /**
     * Registra un nuevo administrador del sistema.
     * Requiere la 'X-System-Key' en el header para validar la autorización de infraestructura.
     */
    @PostMapping("/registro")
    public ResponseEntity<AdministradorResponseDTO> registrar(
            @Valid @RequestBody AdministradorRegistroDTO dto,
            @RequestHeader(value = "X-System-Key", required = false) String systemKey) {
        
        AdministradorResponseDTO nuevoAdmin = administradorService.registrarAdministrador(dto, systemKey);
        return new ResponseEntity<>(nuevoAdmin, HttpStatus.CREATED);
    }

    /**
     * Activa la cuenta desde el enlace del correo y redirige a la SPA (registro exitoso → login admin).
     */
    @GetMapping("/confirmar")
    public RedirectView confirmar(@RequestParam("token") String token) {
        try {
            administradorService.confirmarCuenta(token);
            return confirmRedirect.exitoAdministrador();
        } catch (Exception e) {
            return confirmRedirect.errorConfirmacion("admin", e);
        }
    }

    @PostMapping("/confirmar-codigo")
    public ResponseEntity<?> confirmarConCodigo(@Valid @RequestBody ConfirmarCodigoDTO dto) {
        administradorService.confirmarCuentaConCodigo(dto.getEmail(), dto.getCodigo());
        return ResponseEntity.ok(Map.of("mensaje", "Cuenta confirmada correctamente."));
    }

    @PostMapping("/reenviar-confirmacion")
    public ResponseEntity<?> reenviarConfirmacion(@RequestParam("email") String email) {
        administradorService.reenviarCorreoConfirmacion(email);
        return ResponseEntity.ok().body(
            Map.of("mensaje", "Si el correo existe y no está activado, se ha enviado un nuevo enlace de confirmación.")
        );
    }

    /**
     * Obtiene la lista completa de administradores.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping
    public ResponseEntity<List<AdministradorResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(administradorService.obtenerTodosLosAdministradores());
    }

    /**
     * Obtiene los detalles de un administrador específico por su ID.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(administradorService.obtenerAdministradorPorId(id));
    }

    /**
     * Actualiza la información del administrador. 
     * Registra automáticamente cualquier cambio de estado en la tabla de auditoría.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody AdministradorUpdateDTO dto) {
        return ResponseEntity.ok(administradorService.actualizarAdministrador(id, dto));
    }

    /**
     * Elimina físicamente el registro del administrador.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        administradorService.eliminarAdministrador(id);
        return ResponseEntity.noContent().build();
    }
}