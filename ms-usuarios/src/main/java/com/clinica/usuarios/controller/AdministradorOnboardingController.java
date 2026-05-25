package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.request.ConfirmarCodigoDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;
import com.clinica.usuarios.service.AdministradorService;
import com.clinica.usuarios.web.AccountConfirmationRedirectHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

/**
 * Alta y confirmación de administradores (público, separado del CRUD {@code /api/administradores}).
 * Requiere {@code X-System-Key} solo en {@code POST /registro}.
 */
@RestController
@RequestMapping("/api/onboarding/admin")
@RequiredArgsConstructor
public class AdministradorOnboardingController {

    private final AdministradorService administradorService;
    private final AccountConfirmationRedirectHelper confirmRedirect;

    @PostMapping("/registro")
    public ResponseEntity<AdministradorResponseDTO> registrar(
            @Valid @RequestBody AdministradorRegistroDTO dto,
            @RequestHeader(value = "X-System-Key", required = false) String systemKey) {
        AdministradorResponseDTO nuevoAdmin = administradorService.registrarAdministrador(dto, systemKey);
        return new ResponseEntity<>(nuevoAdmin, HttpStatus.CREATED);
    }

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
        return ResponseEntity.ok(Map.of(
                "mensaje", "Si el correo existe y no está activado, se ha enviado un nuevo enlace de confirmación."));
    }
}
