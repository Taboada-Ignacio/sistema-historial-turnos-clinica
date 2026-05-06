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
public class AdministradorResponseDTO {
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long idUsuario;
    
    private String nombre;
    private String apellido;
    private Integer dni;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    
    // Cambiado de Boolean a String para reflejar el nombre del estado (ACTIVO, PENDIENTE, etc.)
    private String estadoActual; 
    
    private String nombreLocalidad;
    private Set<String> roles;
}