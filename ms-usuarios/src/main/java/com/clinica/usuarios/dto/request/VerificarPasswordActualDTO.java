package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificarPasswordActualDTO {

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
