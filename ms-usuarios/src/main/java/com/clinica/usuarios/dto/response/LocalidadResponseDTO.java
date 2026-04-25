package com.clinica.usuarios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocalidadResponseDTO {
    private Long id;
    private String nombre;
    private Long idProvincia; // Importante para filtrar en el Frontend
}