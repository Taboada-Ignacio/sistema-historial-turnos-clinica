package com.clinica.usuarios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PacienteBusquedaResponseDTO {
    private int total;
    private List<PacienteListadoDTO> pacientes;
    /** Descripción legible de los filtros aplicados (para UI). */
    private String criteriosAplicados;
}
