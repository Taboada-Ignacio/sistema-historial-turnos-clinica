package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.ObraSocialRegistroDTO;
import com.clinica.usuarios.dto.request.ObraSocialUpdateDTO;
import com.clinica.usuarios.dto.response.ObraSocialResponseDTO;
import com.clinica.usuarios.service.ObraSocialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/obras-sociales")
@RequiredArgsConstructor
public class ObraSocialController {

    private final ObraSocialService obraSocialService;

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping("/registro")
    public ResponseEntity<ObraSocialResponseDTO> registrar(@Valid @RequestBody ObraSocialRegistroDTO dto) {
        ObraSocialResponseDTO nuevaObraSocial = obraSocialService.registrarObraSocial(dto);
        return new ResponseEntity<>(nuevaObraSocial, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObraSocialResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(obraSocialService.obtenerObraSocialPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ObraSocialResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(obraSocialService.obtenerTodasLasObrasSociales());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ObraSocialResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody ObraSocialUpdateDTO dto) {
        return ResponseEntity.ok(obraSocialService.actualizarObraSocial(id, dto));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        obraSocialService.eliminarObraSocial(id);
        return ResponseEntity.noContent().build();
    }
}