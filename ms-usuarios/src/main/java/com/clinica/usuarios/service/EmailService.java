package com.clinica.usuarios.service;

import com.clinica.usuarios.model.Usuario;

public interface EmailService {
    void enviarEmailConfirmacion(Usuario usuario, String token, String codigo);
    void enviarEmailAprobacion(Usuario usuario);

    /** Notifica al profesional que su solicitud no fue aprobada (envío síncrono antes del borrado). */
    void enviarEmailRechazoProfesionalPendiente(Usuario usuario, String motivo);

    void enviarEmailRecuperacionPassword(Usuario usuario, String linkRecuperacion, String tipoPortal);

    /** Paciente cargado por profesional: activar cuenta y definir contraseña (solo enlace). */
    void enviarEmailActivacionPaciente(Usuario usuario, String linkActivacion);
}