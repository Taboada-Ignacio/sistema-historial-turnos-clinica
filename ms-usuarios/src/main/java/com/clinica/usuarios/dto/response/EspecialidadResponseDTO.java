package com.clinica.usuarios.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EspecialidadResponseDTO {
    @NotNull(message = "El ID de la especialidad es obligatorio")
    private Long idEspecialidad; 
    private String descripcion;
}