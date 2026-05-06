package com.clinica.usuarios.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProvinciaResponseDTO {
    @NotNull(message = "El ID de la provincia es obligatorio")
    private Long idProvincia;
    private String nombre;
}