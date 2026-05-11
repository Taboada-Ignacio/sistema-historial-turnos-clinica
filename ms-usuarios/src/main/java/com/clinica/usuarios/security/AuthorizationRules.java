package com.clinica.usuarios.security;

import com.clinica.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Expresiones SpEL reutilizables en {@code @PreAuthorize}. El ID de ruta coincide con
 * {@code idUsuario} (herencia JOINED: paciente/profesional/administrador).
 */
@Component("authorizationRules")
@RequiredArgsConstructor
public class AuthorizationRules {

    private final UsuarioRepository usuarioRepository;

    public boolean esMismoUsuario(Long idUsuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .map(u -> u.getIdUsuario().equals(idUsuario))
                .orElse(false);
    }
}
