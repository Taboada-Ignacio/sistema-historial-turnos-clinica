package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.ProvinciaRegistroDTO;
import com.clinica.usuarios.dto.request.ProvinciaUpdateDTO;
import com.clinica.usuarios.dto.response.ProvinciaResponseDTO;
import com.clinica.usuarios.service.ProvinciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provincias")
@RequiredArgsConstructor
public class ProvinciaController {

    private final ProvinciaService provinciaService;

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping("/registro")
    public ResponseEntity<ProvinciaResponseDTO> registrar(@Valid @RequestBody ProvinciaRegistroDTO dto) {
        ProvinciaResponseDTO nuevaProvincia = provinciaService.registrarProvincia(dto);
        return new ResponseEntity<>(nuevaProvincia, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProvinciaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(provinciaService.obtenerProvinciaPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ProvinciaResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(provinciaService.obtenerTodasLasProvincias());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ProvinciaResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody ProvinciaUpdateDTO dto) {
        return ResponseEntity.ok(provinciaService.actualizarProvincia(id, dto));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        provinciaService.eliminarProvincia(id);
        return ResponseEntity.noContent().build();
    }
}