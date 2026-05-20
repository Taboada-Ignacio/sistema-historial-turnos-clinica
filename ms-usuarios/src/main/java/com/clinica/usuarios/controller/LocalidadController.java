package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.LocalidadRegistroDTO;
import com.clinica.usuarios.dto.request.LocalidadUpdateDTO;
import com.clinica.usuarios.dto.response.LocalidadResponseDTO;
import com.clinica.usuarios.service.LocalidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/localidades")
@RequiredArgsConstructor
public class LocalidadController {

    private final LocalidadService localidadService;

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping("/registro")
    public ResponseEntity<LocalidadResponseDTO> registrar(@Valid @RequestBody LocalidadRegistroDTO dto) {
        return new ResponseEntity<>(localidadService.registrarLocalidad(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocalidadResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(localidadService.obtenerLocalidadPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<LocalidadResponseDTO>> listar(
            @RequestParam(required = false) Long provinciaId,
            @RequestParam(required = false) String nombre) {
        return ResponseEntity.ok(localidadService.buscarLocalidades(provinciaId, nombre));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<LocalidadResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody LocalidadUpdateDTO dto) {
        return ResponseEntity.ok(localidadService.actualizarLocalidad(id, dto));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        localidadService.eliminarLocalidad(id);
        return ResponseEntity.noContent().build();
    }
    
    /** Alias de {@code GET /api/localidades?provinciaId=} (compatibilidad). */
    @GetMapping("/provincia/{provinciaId}")
    public ResponseEntity<List<LocalidadResponseDTO>> listarPorProvincia(
            @PathVariable Long provinciaId,
            @RequestParam(required = false) String nombre) {
        return ResponseEntity.ok(localidadService.buscarLocalidades(provinciaId, nombre));
    }
}