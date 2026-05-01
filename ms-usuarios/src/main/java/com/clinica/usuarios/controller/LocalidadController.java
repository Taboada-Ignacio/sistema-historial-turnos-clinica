package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.LocalidadRegistroDTO;
import com.clinica.usuarios.dto.request.LocalidadUpdateDTO;
import com.clinica.usuarios.dto.response.LocalidadResponseDTO;
import com.clinica.usuarios.service.LocalidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/localidades")
@RequiredArgsConstructor
public class LocalidadController {

    private final LocalidadService localidadService;

    @PostMapping("/registro")
    public ResponseEntity<LocalidadResponseDTO> registrar(@Valid @RequestBody LocalidadRegistroDTO dto) {
        return new ResponseEntity<>(localidadService.registrarLocalidad(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocalidadResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(localidadService.obtenerLocalidadPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<LocalidadResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(localidadService.obtenerTodasLasLocalidades());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocalidadResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody LocalidadUpdateDTO dto) {
        return ResponseEntity.ok(localidadService.actualizarLocalidad(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        localidadService.eliminarLocalidad(id);
        return ResponseEntity.noContent().build();
    }
}