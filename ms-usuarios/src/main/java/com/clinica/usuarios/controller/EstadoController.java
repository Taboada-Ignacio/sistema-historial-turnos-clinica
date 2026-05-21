package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.response.EstadoDTO;
import com.clinica.usuarios.service.EstadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catálogo de estados de cuenta: solo lectura (listado / por id).
 */
@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
public class EstadoController {

    private final EstadoService estadoService;

    @GetMapping("/{id}")
    public ResponseEntity<EstadoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(estadoService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<EstadoDTO>> obtenerTodos() {
        return ResponseEntity.ok(estadoService.obtenerTodos());
    }
}
