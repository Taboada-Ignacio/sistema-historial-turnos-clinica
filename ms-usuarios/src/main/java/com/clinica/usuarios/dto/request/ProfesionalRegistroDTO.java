package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class ProfesionalRegistroDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotNull(message = "El DNI es obligatorio")
    private Integer dni;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "Mínimo 8 caracteres")
    private String password;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;

    @NotNull(message = "La matrícula es obligatoria")
    private String matricula;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 500, message = "La dirección no puede superar los 500 caracteres")
    private String direccion;

    @NotNull(message = "La localidad es obligatoria")
    private Long idLocalidad;

    @NotNull(message = "La especialidad es obligatoria")
    private Long idEspecialidad;

    @NotEmpty(message = "El profesional debe tener al menos un rol asignado")
    private Set<Long> rolesIds;

    @Pattern(regexp = "^(?i)(.*\\.webp)$", message = "La foto de perfil debe ser estrictamente en formato .webp")
    private String fotoPerfil;
}