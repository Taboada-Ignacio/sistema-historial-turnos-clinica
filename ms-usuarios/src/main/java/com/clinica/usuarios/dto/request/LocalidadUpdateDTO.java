package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalidadUpdateDTO {
    @NotBlank(message = "El nombre de la localidad es obligatorio")
    private String nombre;

    @NotNull(message = "El ID de la provincia es obligatorio")
    private Long idProvincia;
}