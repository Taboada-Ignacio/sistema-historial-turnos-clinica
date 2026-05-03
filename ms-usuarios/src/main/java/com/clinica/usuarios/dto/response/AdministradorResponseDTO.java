package com.clinica.usuarios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdministradorResponseDTO {
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