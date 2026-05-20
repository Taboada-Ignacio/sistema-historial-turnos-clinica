package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DireccionUpdateDTO {

    @NotBlank(message = "El nombre de la dirección es obligatorio")
    @Size(max = 500, message = "El nombre no puede superar los 500 caracteres")
    private String nombre;
}
