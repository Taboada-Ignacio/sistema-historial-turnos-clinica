package com.clinica.usuarios.service;

import com.clinica.usuarios.model.Usuario;

public interface EmailService {
    void enviarEmailConfirmacion(Usuario usuario, String token);
    void enviarEmailAprobacion(Usuario usuario);
}