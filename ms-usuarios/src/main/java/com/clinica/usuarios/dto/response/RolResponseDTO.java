package com.clinica.usuarios.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RolResponseDTO {
    @NotNull(message = "El ID del rol es obligatorio")
    private Long idRol;
    private String descripcion;
}