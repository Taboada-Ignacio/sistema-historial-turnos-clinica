package com.clinica.usuarios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoDTO {
    private Long idEstado;
    private String nombre; // "PENDIENTE", "ACTIVO", etc.
}