package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;
import com.clinica.usuarios.service.AdministradorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/administradores")
@RequiredArgsConstructor
public class AdministradorController {

    private final AdministradorService administradorService;

    @PostMapping("/registro")
    public ResponseEntity<AdministradorResponseDTO> registrar(@Valid @RequestBody AdministradorRegistroDTO dto) {
        AdministradorResponseDTO nuevoAdmin = administradorService.registrarAdministrador(dto);
        return new ResponseEntity<>(nuevoAdmin, HttpStatus.CREATED);
    }
}