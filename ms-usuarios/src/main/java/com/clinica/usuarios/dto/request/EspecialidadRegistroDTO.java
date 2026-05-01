package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EspecialidadRegistroDTO {

    @NotBlank(message = "La descripción de la especialidad es obligatoria")
    private String descripcion;
}