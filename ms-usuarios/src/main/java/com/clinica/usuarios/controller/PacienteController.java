package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.request.PacienteUpdateDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    /**
     * Endpoint para registrar un nuevo paciente.
     * @Valid activa las anotaciones que pusimos en el DTO (@Email, @NotBlank, etc.)
     */
    @PostMapping("/registro")
    public ResponseEntity<PacienteResponseDTO> registrar(@Valid @RequestBody PacienteRegistroDTO dto) {
        PacienteResponseDTO nuevoPaciente = pacienteService.registrarPaciente(dto);
        // Devolvemos 201 Created con el objeto guardado
        return new ResponseEntity<>(nuevoPaciente, HttpStatus.CREATED);
    }

    /**
     * Endpoint para obtener un paciente específico por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        PacienteResponseDTO paciente = pacienteService.obtenerPacientePorId(id);
        // Devolvemos 200 OK con el DTO del paciente
        return ResponseEntity.ok(paciente); 
    }

    /**
     * Endpoint para obtener el listado completo de pacientes.
     */
    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> obtenerTodos() {
        List<PacienteResponseDTO> pacientes = pacienteService.obtenerTodosLosPacientes();
        // Devolvemos 200 OK con la lista de DTOs
        return ResponseEntity.ok(pacientes); 
    }

    /**
     * Endpoint para actualizar todos los datos de un paciente (excepto su ID y Password).
     * Reemplaza el recurso completo con los datos enviados en el body.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody PacienteUpdateDTO dto) {
        
        // Llamamos al service que hace toda la magia y construye el response con el @Builder
        PacienteResponseDTO pacienteActualizado = pacienteService.actualizarPaciente(id, dto);
        
        // Devolvemos 200 OK con el paciente actualizado y sus relaciones aplanadas
        return ResponseEntity.ok(pacienteActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSoloPaciente(@PathVariable Long id) {
        // CORREGIDO: El nombre correcto de la variable es pacienteService
        pacienteService.eliminarSoloPaciente(id); 
        return ResponseEntity.noContent().build(); 
    }
}