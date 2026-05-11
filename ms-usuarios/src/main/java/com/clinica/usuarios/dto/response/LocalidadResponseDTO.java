package com.clinica.usuarios.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LocalidadResponseDTO {

    @NotNull(message = "El ID de la localidad es obligatorio")
    private Long idLocalidad;

    private String nombre;

    /** ID de provincia (útil para formularios de edición). */
    private Long idProvincia;

    private String nombreProvincia; // Nombre de la provincia aplanado para el frontend
}