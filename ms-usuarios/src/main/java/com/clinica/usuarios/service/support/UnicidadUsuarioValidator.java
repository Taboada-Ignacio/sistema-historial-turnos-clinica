package com.clinica.usuarios.service.support;

import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.repository.UsuarioRepository;

/**
 * Valida email y DNI únicos en alta/actualización de usuarios.
 */
public final class UnicidadUsuarioValidator {

    private UnicidadUsuarioValidator() {
    }

    public static void validarAlta(UsuarioRepository usuarioRepository, String email, Integer dni) {
        boolean emailEnUso = usuarioRepository.findByEmail(email).isPresent();
        boolean dniEnUso = usuarioRepository.findByDni(dni).isPresent();
        lanzarSiConflicto(emailEnUso, dniEnUso, true);
    }

    public static void validarActualizacion(
            UsuarioRepository usuarioRepository, Long idUsuario, String email, Integer dni) {
        boolean emailEnUso = usuarioRepository.findByEmail(email)
                .filter(u -> !u.getIdUsuario().equals(idUsuario))
                .isPresent();
        boolean dniEnUso = usuarioRepository.findByDni(dni)
                .filter(u -> !u.getIdUsuario().equals(idUsuario))
                .isPresent();
        lanzarSiConflicto(emailEnUso, dniEnUso, false);
    }

    private static void lanzarSiConflicto(boolean emailEnUso, boolean dniEnUso, boolean esAlta) {
        if (emailEnUso && dniEnUso) {
            throw new ReglaDeNegocioException(esAlta
                    ? "El correo electrónico y el DNI ya están registrados en el sistema. Utilizá otros datos o iniciá sesión si ya tenés cuenta."
                    : "El correo electrónico y el DNI ya están registrados en otro usuario. Utilizá otros datos.");
        }
        if (emailEnUso) {
            throw new ReglaDeNegocioException(esAlta
                    ? "Este correo electrónico ya está registrado. Utilizá otro email o iniciá sesión si ya tenés cuenta."
                    : "Este correo electrónico ya está registrado por otro usuario. Utilizá otro email.");
        }
        if (dniEnUso) {
            throw new ReglaDeNegocioException(esAlta
                    ? "Este DNI ya está registrado en el sistema. Verificá el número ingresado."
                    : "Este DNI ya está registrado por otro usuario. Verificá el número ingresado.");
        }
    }
}
