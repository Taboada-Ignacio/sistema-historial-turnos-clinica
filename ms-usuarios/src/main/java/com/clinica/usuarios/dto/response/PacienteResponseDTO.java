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
public class PacienteResponseDTO {
    @NotNull(message = "El ID del paciente es obligatorio")
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private Integer dni;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    
    // Cambiado de Boolean a String
    @NotNull(message = "El estado actual es obligatorio")
    private String estadoActual; 
    
    private Long idObraSocial;
    private String nombreObraSocial;
    private String numeroAfiliado;
    private Long idLocalidad;
    private Long idProvincia;
    private String nombreLocalidad;
    private String nombreProvincia;
    /** Texto de la dirección tipificada (catálogo). */
    private String direccion;
    private Set<String> roles;
}