package com.clinica.usuarios.service.support;

import com.clinica.usuarios.model.Usuario;
import com.clinica.usuarios.repository.AdministradorRepository;
import com.clinica.usuarios.repository.PacienteRepository;
import com.clinica.usuarios.repository.ProfesionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Obtiene el nombre de persona asociado a una cuenta {@link Usuario}.
 */
@Component
@RequiredArgsConstructor
public class PersonaNombreResolver {

    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final AdministradorRepository administradorRepository;

    public String resolverNombre(Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() == null) {
            return "Usuario";
        }
        Long id = usuario.getIdUsuario();
        return pacienteRepository.findByUsuario_IdUsuario(id).map(p -> p.getNombre())
                .or(() -> profesionalRepository.findByUsuario_IdUsuario(id).map(p -> p.getNombre()))
                .or(() -> administradorRepository.findByUsuario_IdUsuario(id).map(a -> a.getNombre()))
                .orElse("Usuario");
    }
}
