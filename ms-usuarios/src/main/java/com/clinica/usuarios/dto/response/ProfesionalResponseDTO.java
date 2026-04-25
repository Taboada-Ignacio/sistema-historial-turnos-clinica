package com.clinica.usuarios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

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
    private Integer nroMatricula;
    private String especialidad;
    private String nombreLocalidad;
    private String nombreProvincia;
    private Boolean estado;
}