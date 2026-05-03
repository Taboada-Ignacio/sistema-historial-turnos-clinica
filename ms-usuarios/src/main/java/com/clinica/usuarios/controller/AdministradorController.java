package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.request.AdministradorUpdateDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;
import com.clinica.usuarios.service.AdministradorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
     * Endpoint público para activar la cuenta de administrador mediante el token enviado por correo.
     */
    @GetMapping("/confirmar")
    public ResponseEntity<String> confirmar(@RequestParam("token") String token) {
        administradorService.confirmarCuenta(token);
        return ResponseEntity.ok("Cuenta de administrador activada exitosamente.");
    }

    /**
     * Obtiene la lista completa de administradores.
     */
    @GetMapping
    public ResponseEntity<List<AdministradorResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(administradorService.obtenerTodosLosAdministradores());
    }

    /**
     * Obtiene los detalles de un administrador específico por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(administradorService.obtenerAdministradorPorId(id));
    }

    /**
     * Actualiza la información del administrador. 
     * Registra automáticamente cualquier cambio de estado en la tabla de auditoría.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody AdministradorUpdateDTO dto) {
        return ResponseEntity.ok(administradorService.actualizarAdministrador(id, dto));
    }

    /**
     * Elimina físicamente el registro del administrador.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        administradorService.eliminarAdministrador(id);
        return ResponseEntity.noContent().build();
    }
}