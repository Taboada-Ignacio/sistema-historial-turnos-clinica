package com.clinica.usuarios.service.support;

import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.repository.AdministradorRepository;
import com.clinica.usuarios.repository.PacienteRepository;
import com.clinica.usuarios.repository.ProfesionalRepository;
import com.clinica.usuarios.repository.UsuarioRepository;

/**
 * Valida email único en {@link Usuario} y DNI único global entre pacientes, profesionales y administradores.
 */
public final class UnicidadUsuarioValidator {

    private UnicidadUsuarioValidator() {
    }

    public static void validarAlta(
            UsuarioRepository usuarioRepository,
            PacienteRepository pacienteRepository,
            ProfesionalRepository profesionalRepository,
            AdministradorRepository administradorRepository,
            String email,
            Integer dni) {
        boolean emailEnUso = usuarioRepository.findByEmail(email).isPresent();
        boolean dniEnUso = dniEnUsoGlobal(pacienteRepository, profesionalRepository, administradorRepository, dni, null);
        lanzarSiConflicto(emailEnUso, dniEnUso, true);
    }

    public static void validarActualizacion(
            UsuarioRepository usuarioRepository,
            PacienteRepository pacienteRepository,
            ProfesionalRepository profesionalRepository,
            AdministradorRepository administradorRepository,
            Long idUsuario,
            String email,
            Integer dni) {
        boolean emailEnUso = usuarioRepository.findByEmail(email)
                .filter(u -> !u.getIdUsuario().equals(idUsuario))
                .isPresent();
        boolean dniEnUso = dniEnUsoGlobal(
                pacienteRepository, profesionalRepository, administradorRepository, dni, idUsuario);
        lanzarSiConflicto(emailEnUso, dniEnUso, false);
    }

    private static boolean dniEnUsoGlobal(
            PacienteRepository pacienteRepository,
            ProfesionalRepository profesionalRepository,
            AdministradorRepository administradorRepository,
            Integer dni,
            Long idUsuarioExcluir) {
        return pacienteRepository.findByDni(dni)
                        .filter(p -> !mismoUsuario(p.getIdUsuario(), idUsuarioExcluir))
                        .isPresent()
                || profesionalRepository.findByDni(dni)
                        .filter(p -> !mismoUsuario(p.getIdUsuario(), idUsuarioExcluir))
                        .isPresent()
                || administradorRepository.findByDni(dni)
                        .filter(a -> !mismoUsuario(a.getIdUsuario(), idUsuarioExcluir))
                        .isPresent();
    }

    private static boolean mismoUsuario(Long idEntidad, Long idUsuarioExcluir) {
        return idUsuarioExcluir != null && idUsuarioExcluir.equals(idEntidad);
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
