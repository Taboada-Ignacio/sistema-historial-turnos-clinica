package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.AdministradorUpdateDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;
import com.clinica.usuarios.service.AdministradorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/administradores")
@RequiredArgsConstructor
public class AdministradorController {

    private final AdministradorService administradorService;

    /**
     * Perfil del administrador autenticado (panel admin).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping("/me")
    public ResponseEntity<AdministradorResponseDTO> obtenerSesion(
            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(administradorService.obtenerAdministradorPorEmail(principal.getUsername()));
    }

    /**
     * Listado completo de administradores (son pocos; sin búsqueda paginada).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping
    public ResponseEntity<List<AdministradorResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(administradorService.obtenerTodosLosAdministradores());
    }

    /**
     * Detalle de un administrador (cualquier admin autenticado puede consultar).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<AdministradorResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(administradorService.obtenerAdministradorPorId(id));
    }

    /**
     * Actualiza solo el propio administrador (mismo {@code idUsuario} que el JWT).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') and @authorizationRules.esMismoUsuario(#id)")
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<AdministradorResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AdministradorUpdateDTO dto) {
        return ResponseEntity.ok(administradorService.actualizarAdministrador(id, dto));
    }

    /**
     * Elimina solo la propia cuenta de administrador.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') and @authorizationRules.esMismoUsuario(#id)")
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        administradorService.eliminarAdministrador(id);
        return ResponseEntity.noContent().build();
    }
}