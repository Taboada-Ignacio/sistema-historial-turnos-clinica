package com.clinica.usuarios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CambioMembresiaResponseDTO {
    private Long idCambioMembresia;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaVencimiento;
    private String nombreMembresia; // Ej: "ACTIVA", "SIN_VERIFICAR"
}