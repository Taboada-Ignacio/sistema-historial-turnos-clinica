package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // Aquí irán más adelante los endpoints de GET, PUT, DELETE
}