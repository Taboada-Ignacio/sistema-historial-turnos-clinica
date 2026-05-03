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
     * Registra un nuevo paciente.
     * El estado inicial será 'PENDIENTE' y se disparará el correo.
     */
    @PostMapping("/registro")
    public ResponseEntity<PacienteResponseDTO> registrar(@Valid @RequestBody PacienteRegistroDTO dto) {
        PacienteResponseDTO nuevoPaciente = pacienteService.registrarPaciente(dto);
        return new ResponseEntity<>(nuevoPaciente, HttpStatus.CREATED);
    }

    /**
     * Endpoint público para confirmar la cuenta mediante el token del correo.
     */
    @GetMapping("/confirmar")
    public ResponseEntity<String> confirmarCuenta(@RequestParam("token") String token) {
        pacienteService.confirmarCuenta(token);
        return ResponseEntity.ok("Cuenta confirmada con éxito. Ya podés iniciar sesión.");
    }

    /**
     * Obtiene un paciente por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerPacientePorId(id)); 
    }

    /**
     * Obtiene el listado completo de pacientes.
     * (Útil para el panel de administración de tu clínica).
     */
    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> obtenerTodos() {
        List<PacienteResponseDTO> pacientes = pacienteService.obtenerTodosLosPacientes();
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Actualiza los datos de un paciente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody PacienteUpdateDTO dto) {
        return ResponseEntity.ok(pacienteService.actualizarPaciente(id, dto));
    }

    /**
     * Elimina físicamente a un paciente si no tiene turnos asociados.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminarSoloPaciente(id);
        return ResponseEntity.noContent().build();
    }
}