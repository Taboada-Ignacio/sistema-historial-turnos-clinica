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
public class ProfesionalResponseDTO {
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
    
    // Cambiado de Boolean a String
    private String estadoActual; 
    
    private Set<String> roles;
}