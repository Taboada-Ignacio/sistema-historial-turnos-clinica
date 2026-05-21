package com.clinica.usuarios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfesionalListadoDTO {
    private Long idUsuario;
    private String fotoPerfil;
    private String apellido;
    private String nombre;
    private Integer dni;
    private String especialidad;
}
