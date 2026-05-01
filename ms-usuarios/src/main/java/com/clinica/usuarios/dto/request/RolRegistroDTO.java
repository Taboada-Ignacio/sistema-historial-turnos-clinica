package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolRegistroDTO {

    @NotBlank(message = "La descripción del rol es obligatoria")
    private String descripcion;
}