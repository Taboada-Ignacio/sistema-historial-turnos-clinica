package com.clinica.usuarios.service;

import com.clinica.usuarios.model.Usuario;

public interface VerificationTokenService {

    int HORAS_VALIDEZ = 72;

    record DatosConfirmacion(String token, String codigo) {}

    /** Confirmación de registro (código + enlace). */
    DatosConfirmacion crearTokenConfirmacion(Usuario usuario);

    /** Activación de cuenta cargada por profesional (solo enlace, 72 h). */
    String crearTokenActivacionPaciente(Usuario usuario);

    /** Recuperación de contraseña (solo enlace, 72 h). */
    String crearTokenRecuperacionPassword(Usuario usuario);
}
