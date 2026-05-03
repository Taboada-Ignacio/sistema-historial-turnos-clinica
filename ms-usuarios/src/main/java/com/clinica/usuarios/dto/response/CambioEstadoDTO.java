package com.clinica.usuarios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoDTO {
    private LocalDateTime fecha;
    private String nombreEstado; // Solo el nombre para que el Front lo muestre fácil
}