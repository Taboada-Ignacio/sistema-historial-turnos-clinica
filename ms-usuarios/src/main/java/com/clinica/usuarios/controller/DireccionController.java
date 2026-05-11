package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.DireccionRegistroDTO;
import com.clinica.usuarios.dto.response.DireccionResponseDTO;
import com.clinica.usuarios.service.DireccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/direcciones")
@RequiredArgsConstructor
public class DireccionController {

    private final DireccionService direccionService;

    @GetMapping("/localidad/{idLocalidad}")
    public ResponseEntity<List<DireccionResponseDTO>> listarPorLocalidad(@PathVariable Long idLocalidad) {
        return ResponseEntity.ok(direccionService.listarPorLocalidad(idLocalidad));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping("/registro")
    public ResponseEntity<DireccionResponseDTO> registrar(@Valid @RequestBody DireccionRegistroDTO dto) {
        return new ResponseEntity<>(direccionService.registrar(dto), HttpStatus.CREATED);
    }
}