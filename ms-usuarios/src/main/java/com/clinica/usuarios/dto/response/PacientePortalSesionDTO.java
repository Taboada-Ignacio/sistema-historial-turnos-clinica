package com.clinica.usuarios.dto.response;

import com.clinica.usuarios.model.Sexo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * Sesión del portal paciente: paciente real o profesional con rol paciente (UI limitada).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PacientePortalSesionDTO {
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private Integer dni;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
    private String estadoActual;
    private Long idObraSocial;
    private String nombreObraSocial;
    private String numeroAfiliado;
    private Long idLocalidad;
    private Long idProvincia;
    private String nombreLocalidad;
    private String nombreProvincia;
    private String direccion;
    private Set<String> roles;

    /** {@code PACIENTE} o {@code PROFESIONAL_EN_PORTAL_PACIENTE}. */
    private String tipoCuenta;

    /** Si false, el perfil no se edita vía APIs de paciente (portal profesional). */
    private boolean perfilEditable;
}
