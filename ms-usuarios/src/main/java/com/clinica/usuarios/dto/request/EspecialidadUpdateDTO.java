package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EspecialidadUpdateDTO {

    @NotBlank(message = "La descripción de la especialidad es obligatoria")
    private String descripcion;
}