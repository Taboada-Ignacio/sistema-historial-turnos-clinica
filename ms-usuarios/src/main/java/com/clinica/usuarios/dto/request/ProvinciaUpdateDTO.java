package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProvinciaUpdateDTO {

    @NotBlank(message = "El nombre de la provincia es obligatorio")
    private String nombre;
}