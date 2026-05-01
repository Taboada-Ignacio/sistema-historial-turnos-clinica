package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.request.ProfesionalUpdateDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.service.ProfesionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profesionales")
@RequiredArgsConstructor
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    /**
     * Endpoint para registrar un nuevo profesional.
     */
    @PostMapping("/registro")
    public ResponseEntity<ProfesionalResponseDTO> registrar(@Valid @RequestBody ProfesionalRegistroDTO dto) {
        ProfesionalResponseDTO nuevoProfesional = profesionalService.registrarProfesional(dto);
        // Devolvemos 201 Created con el objeto guardado
        return new ResponseEntity<>(nuevoProfesional, HttpStatus.CREATED);
    }

    /**
     * Endpoint para obtener un profesional específico por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfesionalResponseDTO> obtenerPorId(@PathVariable Long id) {
        ProfesionalResponseDTO profesional = profesionalService.obtenerProfesionalPorId(id);
        // Devolvemos 200 OK con el DTO del profesional
        return ResponseEntity.ok(profesional); 
    }

    /**
     * Endpoint para obtener el listado completo de profesionales.
     */
    @GetMapping
    public ResponseEntity<List<ProfesionalResponseDTO>> obtenerTodos() {
        List<ProfesionalResponseDTO> profesionales = profesionalService.obtenerTodosLosProfesionales();
        // Devolvemos 200 OK con la lista de DTOs
        return ResponseEntity.ok(profesionales); 
    }

    /**
     * Endpoint para actualizar todos los datos de un profesional (excepto su ID y Password).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProfesionalResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody ProfesionalUpdateDTO dto) {
        
        ProfesionalResponseDTO profesionalActualizado = profesionalService.actualizarProfesional(id, dto);
        // Devolvemos 200 OK con el profesional actualizado
        return ResponseEntity.ok(profesionalActualizado);
    }

    /**
     * Endpoint para eliminar físicamente un profesional.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSoloProfesional(@PathVariable Long id) {
        profesionalService.eliminarSoloProfesional(id); 
        // Devolvemos 204 No Content
        return ResponseEntity.noContent().build(); 
    }
}