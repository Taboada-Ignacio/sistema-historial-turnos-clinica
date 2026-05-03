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
     * Registra un nuevo profesional médico.
     * Se asigna el estado 'PENDIENTE' y se envía el correo de confirmación.
     */
    @PostMapping("/registro")
    public ResponseEntity<ProfesionalResponseDTO> registrar(@Valid @RequestBody ProfesionalRegistroDTO dto) {
        ProfesionalResponseDTO nuevoProfesional = profesionalService.registrarProfesional(dto);
        return new ResponseEntity<>(nuevoProfesional, HttpStatus.CREATED);
    }

    /**
     * Endpoint público para la confirmación de identidad vía email.
     * Cambia el estado del profesional a 'ACTIVO'.
     */
    @GetMapping("/confirmar")
    public ResponseEntity<String> confirmar(@RequestParam("token") String token) {
        profesionalService.confirmarCuenta(token);
        return ResponseEntity.ok("Profesional confirmado con éxito. Ahora puede acceder al sistema.");
    }

    /**
     * Retorna el listado completo de profesionales registrados.
     */
    @GetMapping
    public ResponseEntity<List<ProfesionalResponseDTO>> obtenerTodos() {
        List<ProfesionalResponseDTO> profesionales = profesionalService.obtenerTodosLosProfesionales();
        return ResponseEntity.ok(profesionales);
    }

    /**
     * Obtiene la información detallada de un profesional por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfesionalResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(profesionalService.obtenerProfesionalPorId(id));
    }

    /**
     * Actualiza los datos del profesional y registra cambios de estado si corresponde.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProfesionalResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody ProfesionalUpdateDTO dto) {
        return ResponseEntity.ok(profesionalService.actualizarProfesional(id, dto));
    }

    /**
     * Elimina el registro del profesional (Borrado físico).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        profesionalService.eliminarSoloProfesional(id);
        return ResponseEntity.noContent().build();
    }
}