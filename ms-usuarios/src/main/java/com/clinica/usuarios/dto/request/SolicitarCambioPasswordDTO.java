package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SolicitarCambioPasswordDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es valido")
    private String email;
}
