package com.clinica.usuarios.dto.request;

import com.clinica.usuarios.model.Sexo;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PacienteCargaProfesionalDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50)
    private String apellido;

    @NotNull(message = "El DNI es obligatorio")
    @Positive
    private Integer dni;

    @NotBlank(message = "El email es obligatorio")
    @Email
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String telefono;

    @Past
    private LocalDate fechaNacimiento;

    @NotNull(message = "El sexo es obligatorio")
    private Sexo sexo;

    @NotNull(message = "La localidad es obligatoria")
    private Long idLocalidad;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 500)
    private String direccion;

    @NotNull(message = "La obra social es obligatoria")
    private Long idObraSocial;

    private String numeroAfiliado;
}
