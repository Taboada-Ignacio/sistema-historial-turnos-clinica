package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class AdministradorUpdateDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    @NotNull(message = "El DNI es obligatorio")
    private Integer dni;
    @NotBlank(message = "El email es obligatorio")
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String estadoActual; // <--- AGREGAR ESTO
    private Long idLocalidad;
    private Set<Long> rolesIds;
}