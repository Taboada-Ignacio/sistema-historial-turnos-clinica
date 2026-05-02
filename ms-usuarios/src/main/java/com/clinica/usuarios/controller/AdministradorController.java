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

import java.util.List; // <-- ¡Este era el import que faltaba!

@RestController
@RequestMapping("/api/administradores")
@RequiredArgsConstructor
public class AdministradorController {

    private final AdministradorService administradorService;

    @PostMapping("/registro")
    public ResponseEntity<AdministradorResponseDTO> registrar(@Valid @RequestBody AdministradorRegistroDTO dto) {
        AdministradorResponseDTO nuevoAdmin = administradorService.registrarAdministrador(dto);
        return new ResponseEntity<>(nuevoAdmin, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(administradorService.obtenerAdministradorPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<AdministradorResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(administradorService.obtenerTodosLosAdministradores());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody AdministradorUpdateDTO dto) {
        return ResponseEntity.ok(administradorService.actualizarAdministrador(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        administradorService.eliminarAdministrador(id);
        return ResponseEntity.noContent().build();
    }
}