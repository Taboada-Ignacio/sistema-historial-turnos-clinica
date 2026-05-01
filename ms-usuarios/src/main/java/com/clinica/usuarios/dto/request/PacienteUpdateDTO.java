package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

import java.time.LocalDate;

@Getter
@Setter
public class PacienteUpdateDTO {

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

    @NotNull(message = "El ID de la obra social es obligatorio")
    private Long idObraSocial;

    @NotBlank(message = "El número de afiliado es obligatorio")
    private String numeroAfiliado;
    
    private Set<Long> rolesIds;
}