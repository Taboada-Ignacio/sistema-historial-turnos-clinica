package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReenviarAccesoPacienteDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email
    private String email;
}
