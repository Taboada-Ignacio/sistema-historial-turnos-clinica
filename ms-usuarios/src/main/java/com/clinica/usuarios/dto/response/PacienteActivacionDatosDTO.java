package com.clinica.usuarios.dto.response;

import com.clinica.usuarios.model.Sexo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PacienteActivacionDatosDTO {
    private String nombre;
    private String apellido;
    private Integer dni;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
    private String direccion;
    private String nombreLocalidad;
    private String nombreProvincia;
    private String nombreObraSocial;
    private String numeroAfiliado;
}
