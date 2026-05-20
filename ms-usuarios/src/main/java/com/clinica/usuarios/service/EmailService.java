package com.clinica.usuarios.service;

import com.clinica.usuarios.model.Usuario;

public interface EmailService {
    void enviarEmailConfirmacion(Usuario usuario, String token, String codigo);
    void enviarEmailAprobacion(Usuario usuario);
    void enviarEmailRecuperacionPassword(Usuario usuario, String linkRecuperacion, String tipoPortal);
}