package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class ProfesionalUpdateDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    @NotNull(message = "El DNI es obligatorio")
    private Integer dni;
    @NotBlank(message = "El email es obligatorio")
    private String email;
    @Size(max = 500, message = "La dirección no puede superar los 500 caracteres")
    private String direccion;
    private String telefono;
    private String matricula;
    @NotNull(message = "El estado actual es obligatorio")
    private String estadoActual;
    private Long idLocalidad;
    private Long idEspecialidad;
    private Set<Long> rolesIds;
}