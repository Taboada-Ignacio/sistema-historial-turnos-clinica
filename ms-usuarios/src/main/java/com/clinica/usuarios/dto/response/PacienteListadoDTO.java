package com.clinica.usuarios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PacienteListadoDTO {
    private Long idUsuario;
    private String apellido;
    private String nombre;
    private Integer dni;
    private String nombreProvincia;
    private String nombreLocalidad;
}
