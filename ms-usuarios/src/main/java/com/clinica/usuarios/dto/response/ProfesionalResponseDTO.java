package com.clinica.usuarios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfesionalResponseDTO {
    @NotNull(message = "El ID del profesional es obligatorio")
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private Integer dni;               
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String matricula;
    private String especialidad;
    private String nombreLocalidad;
    private String nombreProvincia;
    
    @NotNull(message = "El estado actual es obligatorio")
    private String estadoActual; 
    
    // --- NUEVO CAMPO ---
    private String fotoPerfil;
    
    // --- NUEVO CAMPO ---
    private String membresiaActual; 
    
    private Set<String> roles;
}