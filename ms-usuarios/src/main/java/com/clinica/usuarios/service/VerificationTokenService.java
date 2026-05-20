package com.clinica.usuarios.service;

import com.clinica.usuarios.model.Usuario;

public interface VerificationTokenService {

    record DatosConfirmacion(String token, String codigo) {}

    DatosConfirmacion crearTokenConfirmacion(Usuario usuario);
}
