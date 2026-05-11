package com.clinica.usuarios.web;

import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Redirecciones HTTP tras confirmar cuenta por email (SPA). La URL del front se configura con {@code app.frontend-url}.
 */
@Component
public class AccountConfirmationRedirectHelper {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public RedirectView exitoProfesional() {
        return new RedirectView(frontendUrl + "/aprobacion-pendiente");
    }

    public RedirectView exitoPaciente() {
        return new RedirectView(frontendUrl + "/registro-exitoso-paciente");
    }

    public RedirectView exitoAdministrador() {
        return new RedirectView(frontendUrl + "/registro-exitoso-admin");
    }

    /**
     * @param portal paciente | profesional | admin
     */
    public RedirectView errorConfirmacion(String portal, Exception e) {
        String motivo = "invalido";
        if (e instanceof RecursoNoEncontradoException) {
            motivo = "invalido";
        } else if (e instanceof ReglaDeNegocioException re) {
            String msg = re.getMessage() != null ? re.getMessage().toLowerCase() : "";
            motivo = msg.contains("expir") ? "expirado" : "invalido";
        }
        return new RedirectView(frontendUrl + "/confirmacion-error?tipo=" + portal + "&motivo=" + motivo);
    }
}
