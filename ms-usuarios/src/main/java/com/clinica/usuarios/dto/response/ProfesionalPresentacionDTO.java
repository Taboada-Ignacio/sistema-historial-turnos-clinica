package com.clinica.usuarios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos públicos de un profesional para catálogo / fichas (sin datos sensibles).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalPresentacionDTO {

    private Long idUsuario;
    private String nombre;
    private String apellido;
    /** Nombre de la especialidad clínica. */
    private String especialidad;
    /**
     * Texto compuesto: {@code calle (si existe) - localidad - provincia}.
     */
    private String direccion;
    /** Ruta relativa servida por el backend (ej. {@code /fotosPerfilProfesionales/...}); puede ser null. */
    private String fotoPerfil;
}
