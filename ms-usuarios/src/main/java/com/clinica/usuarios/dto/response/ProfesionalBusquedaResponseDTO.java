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
public class ProfesionalBusquedaResponseDTO {
    private int total;
    private List<ProfesionalListadoDTO> profesionales;
    private String criteriosAplicados;
}
