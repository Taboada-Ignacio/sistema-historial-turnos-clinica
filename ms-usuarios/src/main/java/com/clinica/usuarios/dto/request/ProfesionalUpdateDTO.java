package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;

@Getter
@Setter
public class ProfesionalUpdateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotNull(message = "El DNI es obligatorio")
    private Integer dni;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email debe ser válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El estado (activo/inactivo) es obligatorio")
    private Boolean estado;

    @NotNull(message = "El ID de la localidad es obligatorio")
    private Long idLocalidad;

    // --- Campos específicos de Profesional ---

    @NotNull(message = "El ID de la especialidad es obligatorio")
    private Long idEspecialidad;

    @NotBlank(message = "La matrícula es obligatoria")
    private String matricula;

    @NotEmpty(message = "El profesional debe tener al menos un rol asignado")
    private Set<Long> rolesIds;
}