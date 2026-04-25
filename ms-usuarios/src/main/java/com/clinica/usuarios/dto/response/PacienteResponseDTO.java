package com.clinica.usuarios.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder // Muy útil para construir la respuesta en el Service
public class PacienteResponseDTO {
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private Integer dni;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private Boolean estado;
    
    // Aplanamos las relaciones para hacerle la vida más fácil al Frontend de React
    private String nombreObraSocial;
    private String numeroAfiliado;
    private String nombreLocalidad;
    private String nombreProvincia;
}