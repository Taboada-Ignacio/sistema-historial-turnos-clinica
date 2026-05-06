package com.clinica.usuarios.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ObraSocialResponseDTO {
    @NotNull(message = "El ID de la obra social es obligatorio")
    private Long idObraSocial; 
    private String descripcion;
}