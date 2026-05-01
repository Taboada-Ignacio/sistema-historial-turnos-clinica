package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.EspecialidadRegistroDTO;
import com.clinica.usuarios.dto.request.EspecialidadUpdateDTO;
import com.clinica.usuarios.dto.response.EspecialidadResponseDTO;
import com.clinica.usuarios.service.EspecialidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    @PostMapping("/registro")
    public ResponseEntity<EspecialidadResponseDTO> registrar(@Valid @RequestBody EspecialidadRegistroDTO dto) {
        EspecialidadResponseDTO nuevaEspecialidad = especialidadService.registrarEspecialidad(dto);
        return new ResponseEntity<>(nuevaEspecialidad, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadService.obtenerEspecialidadPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<EspecialidadResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(especialidadService.obtenerTodasLasEspecialidades());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody EspecialidadUpdateDTO dto) {
        return ResponseEntity.ok(especialidadService.actualizarEspecialidad(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        especialidadService.eliminarEspecialidad(id);
        return ResponseEntity.noContent().build();
    }
}
