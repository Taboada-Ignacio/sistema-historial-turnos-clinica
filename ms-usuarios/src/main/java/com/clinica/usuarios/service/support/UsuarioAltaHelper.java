package com.clinica.usuarios.service.support;

import com.clinica.usuarios.model.Estado;
import com.clinica.usuarios.model.Rol;
import com.clinica.usuarios.model.Usuario;

import java.util.Set;

public final class UsuarioAltaHelper {

    private UsuarioAltaHelper() {
    }

    public static Usuario nuevoUsuario(String email, String passwordHash, Estado estado, Set<Rol> roles) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordHash);
        usuario.setEstadoActual(estado);
        usuario.setRoles(roles);
        return usuario;
    }
}
