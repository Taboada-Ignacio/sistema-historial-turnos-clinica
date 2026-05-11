package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    /** Calle y número (opcional). */
    private String direccion;
    private String telefono;
    private String matricula;
    @NotNull(message = "El estado actual es obligatorio")
    private String estadoActual;
    private Long idLocalidad;
    private Long idEspecialidad;
    private Set<Long> rolesIds;
    
    @Pattern(regexp = "^(?i)(.*\\.webp)$", message = "La foto de perfil debe ser estrictamente en formato .webp")
    private String fotoPerfil;
}