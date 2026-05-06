package com.clinica.usuarios.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoDTO {
    @NotNull(message = "El ID del estado es obligatorio")
    private Long idEstado;
    private String nombre; // "PENDIENTE", "ACTIVO", etc.
}