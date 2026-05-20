package com.clinica.usuarios.service;

import com.clinica.usuarios.model.VerificationToken;

import java.util.function.Consumer;

public interface AccountActivationService {

    void confirmarCuentaDesdeToken(VerificationToken verificationToken, Consumer<com.clinica.usuarios.model.Usuario> registrarHistorialEstado);
}
