package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RechazarProfesionalPendienteDTO {

    @NotBlank(message = "El motivo del rechazo es obligatorio")
    @Size(min = 10, max = 1000, message = "El motivo debe tener entre 10 y 1000 caracteres")
    private String motivo;

    @NotBlank(message = "La contraseña del administrador es obligatoria")
    private String password;
}
