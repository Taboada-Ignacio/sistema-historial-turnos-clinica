package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObraSocialUpdateDTO {

    @NotBlank(message = "El nombre de la Obra Social es obligatorio")
    private String descripcion;
}