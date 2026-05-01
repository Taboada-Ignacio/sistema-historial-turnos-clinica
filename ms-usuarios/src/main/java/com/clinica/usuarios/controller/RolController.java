package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.RolRegistroDTO;
import com.clinica.usuarios.dto.request.RolUpdateDTO;
import com.clinica.usuarios.dto.response.RolResponseDTO;
import com.clinica.usuarios.service.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @PostMapping("/registro")
    public ResponseEntity<RolResponseDTO> registrar(@Valid @RequestBody RolRegistroDTO dto) {
        RolResponseDTO nuevoRol = rolService.registrarRol(dto);
        return new ResponseEntity<>(nuevoRol, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.obtenerRolPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(rolService.obtenerTodosLosRoles());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolResponseDTO> actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody RolUpdateDTO dto) {
        return ResponseEntity.ok(rolService.actualizarRol(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        rolService.eliminarRol(id);
        return ResponseEntity.noContent().build();
    }
}