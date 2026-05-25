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
public class PersonaEnZonaDetalleDTO {
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private Integer dni;
    private String telefono;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
    private String nombreLocalidad;
    private String nombreProvincia;
    private String direccion;
    /** PACIENTE, PROFESIONAL o ADMINISTRADOR. */
    private String tipoCuenta;
}
