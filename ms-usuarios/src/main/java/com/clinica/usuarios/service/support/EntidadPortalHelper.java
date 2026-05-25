package com.clinica.usuarios.service.support;

import com.clinica.usuarios.model.Rol;
import com.clinica.usuarios.model.Usuario;
import com.clinica.usuarios.repository.AdministradorRepository;
import com.clinica.usuarios.repository.PacienteRepository;
import com.clinica.usuarios.repository.ProfesionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntidadPortalHelper {

    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final AdministradorRepository administradorRepository;

    public boolean puedeAccederPortal(Usuario usuario, String portal) {
        if (usuario == null || usuario.getIdUsuario() == null) {
            return false;
        }
        Long id = usuario.getIdUsuario();
        return switch (portal) {
            case "paciente" -> pacienteRepository.findByUsuario_IdUsuario(id).isPresent()
                    || (profesionalRepository.findByUsuario_IdUsuario(id).isPresent()
                            && tieneRol(usuario, "ROLE_PACIENTE"));
            case "profesional" -> profesionalRepository.findByUsuario_IdUsuario(id).isPresent()
                    && tieneRol(usuario, "ROLE_PROFESIONAL");
            case "admin" -> administradorRepository.findByUsuario_IdUsuario(id).isPresent();
            default -> false;
        };
    }

    public boolean esPaciente(Usuario usuario) {
        return usuario != null
                && usuario.getIdUsuario() != null
                && pacienteRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).isPresent();
    }

    public boolean esProfesional(Usuario usuario) {
        return usuario != null
                && usuario.getIdUsuario() != null
                && profesionalRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).isPresent();
    }

    public boolean esAdministrador(Usuario usuario) {
        return usuario != null
                && usuario.getIdUsuario() != null
                && administradorRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).isPresent();
    }

    public static boolean tieneRol(Usuario usuario, String rol) {
        return usuario.getRoles() != null
                && usuario.getRoles().stream().anyMatch(r -> rol.equals(r.getDescripcion()));
    }
}
