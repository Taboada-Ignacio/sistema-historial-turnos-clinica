package com.clinica.usuarios.dto.request;

import com.clinica.usuarios.model.Sexo;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;
import jakarta.validation.constraints.NotEmpty;

@Data
public class PacienteRegistroDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;

    @NotNull(message = "El DNI es obligatorio")
    @Positive(message = "El DNI debe ser un número positivo")
    private Integer dni;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "El formato del teléfono no es válido")
    private String telefono;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El sexo es obligatorio")
    private Sexo sexo;

    @NotNull(message = "La localidad es obligatoria")
    private Long idLocalidad;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 500, message = "La dirección no puede superar los 500 caracteres")
    private String direccion;

    @NotNull(message = "La obra social es obligatoria")
    private Long idObraSocial;

    // Al no poner @NotBlank ni @NotNull, Spring Validation permite que llegue vacío o nulo
    private String numeroAfiliado;

    @NotEmpty(message = "El paciente debe tener al menos un rol asignado")
    private Set<Long> rolesIds;
}