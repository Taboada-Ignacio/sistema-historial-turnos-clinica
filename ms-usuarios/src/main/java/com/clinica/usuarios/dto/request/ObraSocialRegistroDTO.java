package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObraSocialRegistroDTO {

    @NotBlank(message = "La descripción de la Obra Social es obligatoria")
    private String descripcion;
}