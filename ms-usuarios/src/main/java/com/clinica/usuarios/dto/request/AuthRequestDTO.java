package com.clinica.usuarios.dto.request;

import lombok.Data;

@Data
public class AuthRequestDTO {
    private String email;
    private String password;
    /** Obligatorio en login: {@code paciente}, {@code profesional} o {@code admin}. */
    private String portal;
}