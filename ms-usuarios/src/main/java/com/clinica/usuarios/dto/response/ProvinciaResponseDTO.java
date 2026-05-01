package com.clinica.usuarios.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProvinciaResponseDTO {
    private Long idProvincia;
    private String nombre;
}