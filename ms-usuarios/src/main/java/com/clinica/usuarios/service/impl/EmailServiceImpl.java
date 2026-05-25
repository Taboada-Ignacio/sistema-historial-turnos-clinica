package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.model.Usuario;
import com.clinica.usuarios.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // Asegurate que en application.yml app.url sea http://localhost:8080
    @Value("${app.url}")
    private String appUrl;

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    @Async
    public void enviarEmailConfirmacion(Usuario usuario, String token, String codigo) {
        try {
            // 1. Determinar el endpoint según el rol (Priorizando Administradores)
            String endpoint = "pacientes"; // Default
            
            boolean esAdmin = usuario.getRoles().stream()
                    .anyMatch(r -> r.getDescripcion().equals("ROLE_ADMINISTRADOR"));
            boolean esProfesional = usuario.getRoles().stream()
                    .anyMatch(r -> r.getDescripcion().equals("ROLE_PROFESIONAL"));

            if (esAdmin) {
                endpoint = "onboarding/admin";
            } else if (esProfesional) {
                endpoint = "profesionales";
            }

            // 2. IMPORTANTE: Agregamos el prefijo /usuarios para que el API Gateway (8080) 
            // reconozca la ruta y la derive al microservicio correcto.
            String linkConfirmacion = String.format("%s/usuarios/api/%s/confirmar?token=%s", appUrl, endpoint, token);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Personalizar el contenido según el tipo de usuario
            String htmlMsg;
            if (esProfesional) {
                htmlMsg = generarEmailProfesional(usuario, codigo, linkConfirmacion);
            } else if (esAdmin) {
                htmlMsg = generarEmailAdministrador(usuario, codigo, linkConfirmacion);
            } else {
                htmlMsg = generarEmailPaciente(usuario, codigo, linkConfirmacion);
            }

            helper.setText(htmlMsg, true);
            helper.setTo(usuario.getEmail());
            helper.setSubject("Confirmación de registro - Clínica UTN");
            helper.setFrom(sender);

            mailSender.send(mimeMessage);
            log.info("Email de confirmación generado y enviado a: {}", usuario.getEmail());

        } catch (MessagingException e) {
            log.error("Fallo crítico al enviar el email a {}: {}", usuario.getEmail(), e.getMessage());
        }
    }

    @Override
    @Async
    public void enviarEmailAprobacion(Usuario usuario) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = String.format(
                "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #2ecc71; max-width: 600px; margin: auto; border-radius: 8px;'>" +
                "<h2 style='color: #27ae60; text-align: center;'>✓ ¡Aprobado!</h2>" +
                "<p>Hola <strong>%s</strong>,</p>" +
                "<p>Tu solicitud de acceso ha sido <strong>aprobada</strong> por Administración.</p>" +
                "<p>Ahora podés acceder al sistema completo con todas sus funcionalidades.</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "  <a href='%s' style='background-color: #27ae60; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Acceder al Sistema</a>" +
                "</div>" +
                "<hr style='border: 0; border-top: 1px solid #ecf0f1; margin-top: 30px;'>" +
                "<p style='font-size: 0.8em; color: #777; text-align: center;'>Este es un correo automático del Sistema de Gestión de Historial Clínico.</p>" +
                "</div>",
                usuario.getNombre(), appUrl
            );

            helper.setText(htmlMsg, true);
            helper.setTo(usuario.getEmail());
            helper.setSubject("¡Tu cuenta ha sido aprobada! - Clínica UTN");
            helper.setFrom(sender);

            mailSender.send(mimeMessage);
            log.info("Email de aprobación enviado a: {}", usuario.getEmail());

        } catch (MessagingException e) {
            log.error("Error al enviar email de aprobación a {}: {}", usuario.getEmail(), e.getMessage());
        }
    }

    @Override
    public void enviarEmailRechazoProfesionalPendiente(Usuario usuario, String motivo) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String motivoSeguro = motivo == null ? "" : motivo.trim();
            String htmlMsg = String.format(
                "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e74c3c; max-width: 600px; margin: auto; border-radius: 8px;'>" +
                "<h2 style='color: #c0392b; text-align: center;'>Solicitud no aprobada</h2>" +
                "<p>Hola <strong>%s</strong>,</p>" +
                "<p>Te informamos que tu solicitud de alta como profesional en la <strong>Clínica UTN</strong> " +
                "<strong>no ha sido aprobada</strong> por el equipo de Administración.</p>" +
                "<div style='background-color: #fdf2f2; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #e74c3c;'>" +
                "  <p style='margin: 0 0 6px; font-size: 0.85em; color: #777; text-transform: uppercase;'>Motivo informado</p>" +
                "  <p style='margin: 0; color: #333; white-space: pre-wrap;'>%s</p>" +
                "</div>" +
                "<p style='font-size: 0.9em; color: #555;'>Si considerás que se trata de un error, podés contactar a la clínica para más información.</p>" +
                "<hr style='border: 0; border-top: 1px solid #ecf0f1; margin-top: 30px;'>" +
                "<p style='font-size: 0.8em; color: #777; text-align: center;'>Este es un correo automático del Sistema de Gestión de Historial Clínico.</p>" +
                "</div>",
                usuario.getNombre(), motivoSeguro.replace("<", "&lt;").replace(">", "&gt;")
            );

            helper.setText(htmlMsg, true);
            helper.setTo(usuario.getEmail());
            helper.setSubject("Solicitud profesional no aprobada - Clínica UTN");
            helper.setFrom(sender);

            mailSender.send(mimeMessage);
            log.info("Email de rechazo de solicitud profesional enviado a: {}", usuario.getEmail());
        } catch (MessagingException e) {
            log.error("Error al enviar email de rechazo a {}: {}", usuario.getEmail(), e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de notificación al profesional.", e);
        }
    }

    @Override
    @Async
    public void enviarEmailRecuperacionPassword(Usuario usuario, String linkRecuperacion, String tipoPortal) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String tipoNormalizado = tipoPortal == null ? "" : tipoPortal.trim().toLowerCase();
            boolean esProfesional = "profesional".equalsIgnoreCase(tipoNormalizado);
            boolean esAdmin = "admin".equalsIgnoreCase(tipoNormalizado);

            String color = esAdmin ? "#e11d48" : (esProfesional ? "#2563eb" : "#15803d");
            String titulo = esAdmin
                    ? "Recuperación de acceso de administrador"
                    : (esProfesional ? "Recuperación de acceso profesional" : "Recuperación de acceso paciente");
            String boton = esAdmin
                    ? "Confirmar recuperación de administrador"
                    : (esProfesional ? "Confirmar recuperación profesional" : "Confirmar recuperación de cuenta");

            String htmlMsg = String.format(
                    "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid %s; max-width: 600px; margin: auto; border-radius: 8px;'>" +
                            "<h2 style='color: %s; text-align: center;'>%s</h2>" +
                            "<p>Hola <strong>%s</strong>,</p>" +
                            "<p>Recibimos una solicitud para cambiar tu contraseña.</p>" +
                            "<p>Si fuiste vos, confirmá el proceso desde el siguiente botón:</p>" +
                            "<div style='text-align: center; margin: 30px 0;'>" +
                            "  <a href='%s' style='background-color: %s; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>%s</a>" +
                            "</div>" +
                            "<p style='font-size: 0.9em; color: #555;'>Si no solicitaste este cambio, ignorá este correo.</p>" +
                            "<p style='word-break: break-all; color: #3498db; font-size: 0.85em;'>%s</p>" +
                            "<hr style='border: 0; border-top: 1px solid #ecf0f1; margin-top: 30px;'>" +
                            "<p style='font-size: 0.8em; color: #777; text-align: center;'>Este enlace es válido por 72 horas. Si expira, solicitá uno nuevo desde la pantalla de recuperación.</p>" +
                            "</div>",
                    color, color, titulo, usuario.getNombre(), linkRecuperacion, color, boton, linkRecuperacion
            );

            helper.setText(htmlMsg, true);
            helper.setTo(usuario.getEmail());
            helper.setSubject("Recuperación de contraseña - Clínica UTN");
            helper.setFrom(sender);

            mailSender.send(mimeMessage);
            log.info("Email de recuperación enviado a: {}", usuario.getEmail());
        } catch (MessagingException e) {
            log.error("Error al enviar email de recuperación a {}: {}", usuario.getEmail(), e.getMessage());
        }
    }

    private String bloqueCodigoVerificacion(String codigo, String color) {
        return String.format(
            "<div style='text-align: center; margin: 24px 0; padding: 20px; background-color: #f8fafc; border-radius: 8px;'>"
                    + "<p style='margin: 0 0 8px; font-size: 14px; color: #555;'>Tu código de verificación es:</p>"
                    + "<p style='margin: 0; font-size: 36px; font-weight: bold; letter-spacing: 10px; color: %s;'>%s</p>"
                    + "<p style='margin: 12px 0 0; font-size: 12px; color: #777;'>Válido por <strong>72 horas</strong>. Ingresalo en la pantalla de verificación.</p>"
                    + "</div>",
            color, codigo);
    }

    private String generarEmailProfesional(Usuario usuario, String codigo, String linkConfirmacion) {
        String bloqueCodigo = bloqueCodigoVerificacion(codigo, "#3498db");
        return String.format(
            "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #3498db; max-width: 600px; margin: auto; border-radius: 8px;'>" +
            "<div style='text-align: center; margin-bottom: 20px;'>" +
            "  <h2 style='color: #2c3e50;'>Bienvenido, Profesional</h2>" +
            "</div>" +
            "<p>Hola <strong>%s</strong>,</p>" +
            "<p>¡Gracias por registrarte en el portal profesional de la <strong>Clínica UTN</strong>!</p>" +
            "%s" +
            "<p style='font-size: 0.95em; color: #555; margin-top: 20px;'>También podés confirmar con un clic:</p>" +
            "<div style='text-align: center; margin: 30px 0;'>" +
            "  <a href='%s' style='background-color: #3498db; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Confirmar mi Email</a>" +
            "</div>" +
            "<div style='background-color: #ecf0f1; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
            "  <p style='margin: 0; font-size: 0.9em; color: #555;'><strong>Próximo paso:</strong> Una vez confirmado, tu solicitud será revisada por Administración.</p>" +
            "</div>" +
            "<p style='font-size: 0.9em; color: #555;'>Si el botón no funciona, copiá este link:</p>" +
            "<p style='word-break: break-all; color: #3498db; font-size: 0.85em;'>%s</p>" +
            "<hr style='border: 0; border-top: 1px solid #ecf0f1; margin-top: 30px;'>" +
            "<p style='font-size: 0.8em; color: #777; text-align: center;'>Este es un correo automático. No respondas a este mensaje.</p>" +
            "</div>",
            usuario.getNombre(), bloqueCodigo, linkConfirmacion, linkConfirmacion
        );
    }

    private String generarEmailAdministrador(Usuario usuario, String codigo, String linkConfirmacion) {
        String bloqueCodigo = bloqueCodigoVerificacion(codigo, "#e74c3c");
        return String.format(
            "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e74c3c; max-width: 600px; margin: auto; border-radius: 8px;'>" +
            "<div style='text-align: center; margin-bottom: 20px;'>" +
            "  <h2 style='color: #c0392b;'>Panel de Administrador</h2>" +
            "</div>" +
            "<p>Hola <strong>%s</strong>,</p>" +
            "<p>Tu cuenta de administrador ha sido creada en la <strong>Clínica UTN</strong>.</p>" +
            "%s" +
            "<p style='font-size: 0.95em; color: #555; margin-top: 20px;'>También podés confirmar con un clic:</p>" +
            "<div style='text-align: center; margin: 30px 0;'>" +
            "  <a href='%s' style='background-color: #e74c3c; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Activar Cuenta Admin</a>" +
            "</div>" +
            "<p style='font-size: 0.9em; color: #555;'>Si el botón no funciona, copiá este link:</p>" +
            "<p style='word-break: break-all; color: #3498db; font-size: 0.85em;'>%s</p>" +
            "<hr style='border: 0; border-top: 1px solid #ecf0f1; margin-top: 30px;'>" +
            "<p style='font-size: 0.8em; color: #777; text-align: center;'>Este es un correo automático. No respondas a este mensaje.</p>" +
            "</div>",
            usuario.getNombre(), bloqueCodigo, linkConfirmacion, linkConfirmacion
        );
    }

    @Override
    @Async
    public void enviarEmailActivacionPaciente(Usuario usuario, String linkActivacion) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = String.format(
                    "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #27ae60; max-width: 600px; margin: auto; border-radius: 8px;'>"
                            + "<h2 style='color: #27ae60;'>Activá tu cuenta de paciente</h2>"
                            + "<p>Hola <strong>%s</strong>,</p>"
                            + "<p>Un profesional de la <strong>Clínica UTN</strong> registró tu usuario en el sistema. "
                            + "Para ingresar al portal, creá tu contraseña desde el siguiente enlace:</p>"
                            + "<div style='text-align: center; margin: 30px 0;'>"
                            + "  <a href='%s' style='background-color: #27ae60; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Crear mi contraseña</a>"
                            + "</div>"
                            + "<p style='font-size: 0.9em; color: #555;'>Si el botón no funciona, copiá este link:</p>"
                            + "<p style='word-break: break-all; color: #3498db; font-size: 0.85em;'>%s</p>"
                            + "<hr style='border: 0; border-top: 1px solid #ecf0f1; margin-top: 30px;'>"
                            + "<p style='font-size: 0.8em; color: #777; text-align: center;'>El enlace es válido por 72 horas.</p>"
                            + "</div>",
                    usuario.getNombre(), linkActivacion, linkActivacion);

            helper.setText(htmlMsg, true);
            helper.setTo(usuario.getEmail());
            helper.setSubject("Activá tu cuenta - Clínica UTN");
            helper.setFrom(sender);
            mailSender.send(mimeMessage);
            log.info("Email de activación de paciente enviado a: {}", usuario.getEmail());
        } catch (MessagingException e) {
            log.error("Error al enviar email de activación a {}: {}", usuario.getEmail(), e.getMessage());
        }
    }

    private String generarEmailPaciente(Usuario usuario, String codigo, String linkConfirmacion) {
        String bloqueCodigo = bloqueCodigoVerificacion(codigo, "#27ae60");
        return String.format(
            "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #27ae60; max-width: 600px; margin: auto; border-radius: 8px;'>" +
            "<h2 style='color: #27ae60;'>Bienvenido, Paciente</h2>" +
            "<p>¡Hola <strong>%s</strong>!</p>" +
            "<p>Gracias por registrarte en el sistema de la <strong>Clínica UTN</strong>.</p>" +
            "%s" +
            "<p style='font-size: 0.95em; color: #555; margin-top: 20px;'>También podés confirmar con un clic:</p>" +
            "<div style='text-align: center; margin: 30px 0;'>" +
            "  <a href='%s' style='background-color: #27ae60; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Confirmar mi Cuenta</a>" +
            "</div>" +
            "<p style='font-size: 0.9em; color: #555;'>Si el botón no funciona, copiá este link:</p>" +
            "<p style='word-break: break-all; color: #3498db; font-size: 0.85em;'>%s</p>" +
            "<hr style='border: 0; border-top: 1px solid #ecf0f1; margin-top: 30px;'>" +
            "<p style='font-size: 0.8em; color: #777; text-align: center;'>Este es un correo automático del Sistema de Gestión de Historial Clínico.</p>" +
            "</div>",
            usuario.getNombre(), bloqueCodigo, linkConfirmacion, linkConfirmacion
        );
    }
}