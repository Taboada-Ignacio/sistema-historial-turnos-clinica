package com.clinica.usuarios.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class AdministradorResponseDTO {
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private Integer dni;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private Boolean estado;
    private String nombreLocalidad;
    private Set<String> roles;
}