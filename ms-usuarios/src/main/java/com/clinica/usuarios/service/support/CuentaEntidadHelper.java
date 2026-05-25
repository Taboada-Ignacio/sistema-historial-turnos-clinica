package com.clinica.usuarios.service.support;

import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.AdministradorRepository;
import com.clinica.usuarios.repository.PacienteRepository;
import com.clinica.usuarios.repository.ProfesionalRepository;
import com.clinica.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class CuentaEntidadHelper {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final AdministradorRepository administradorRepository;

    public void validarAlta(String email, Integer dni) {
        UnicidadUsuarioValidator.validarAlta(
                usuarioRepository, pacienteRepository, profesionalRepository, administradorRepository, email, dni);
    }

    public void validarActualizacion(Long idUsuario, String email, Integer dni) {
        UnicidadUsuarioValidator.validarActualizacion(
                usuarioRepository, pacienteRepository, profesionalRepository, administradorRepository,
                idUsuario, email, dni);
    }

    public Paciente guardarPaciente(Paciente paciente, String email, String passwordHash, Estado estado, Set<Rol> roles) {
        Usuario usuario = usuarioRepository.save(UsuarioAltaHelper.nuevoUsuario(email, passwordHash, estado, roles));
        paciente.setUsuario(usuario);
        return pacienteRepository.save(paciente);
    }

    public Profesional guardarProfesional(
            Profesional profesional, String email, String passwordHash, Estado estado, Set<Rol> roles) {
        Usuario usuario = usuarioRepository.save(UsuarioAltaHelper.nuevoUsuario(email, passwordHash, estado, roles));
        profesional.setUsuario(usuario);
        return profesionalRepository.save(profesional);
    }

    public Administrador guardarAdministrador(
            Administrador admin, String email, String passwordHash, Estado estado, Set<Rol> roles) {
        Usuario usuario = usuarioRepository.save(UsuarioAltaHelper.nuevoUsuario(email, passwordHash, estado, roles));
        admin.setUsuario(usuario);
        return administradorRepository.save(admin);
    }

    public static String sexoAsString(Sexo sexo) {
        return sexo != null ? sexo.name() : null;
    }

    public static Sexo sexoAsEnum(String sexo) {
        return sexo != null && !sexo.isBlank() ? Sexo.valueOf(sexo) : null;
    }

    public static String nombreEstado(Usuario usuario) {
        return usuario.getEstadoActual() != null ? usuario.getEstadoActual().getNombre() : "";
    }

    public static String nombreEstado(Paciente paciente) {
        return nombreEstado(paciente.getUsuario());
    }

    public static String emailDe(Paciente paciente) {
        return paciente.getUsuario().getEmail();
    }

    public static String emailDe(Profesional profesional) {
        return profesional.getUsuario().getEmail();
    }
}
