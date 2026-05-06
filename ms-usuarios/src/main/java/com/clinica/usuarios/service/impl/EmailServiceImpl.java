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
    public void enviarEmailConfirmacion(Usuario usuario, String token) {
        try {
            // 1. Determinar el endpoint según el rol (Priorizando Administradores)
            String endpoint = "pacientes"; // Default
            String tipoUsuario = "Paciente";
            
            boolean esAdmin = usuario.getRoles().stream()
                    .anyMatch(r -> r.getDescripcion().equals("ROLE_ADMINISTRADOR"));
            boolean esProfesional = usuario.getRoles().stream()
                    .anyMatch(r -> r.getDescripcion().equals("ROLE_PROFESIONAL"));

            if (esAdmin) {
                endpoint = "administradores";
                tipoUsuario = "Administrador";
            } else if (esProfesional) {
                endpoint = "profesionales";
                tipoUsuario = "Profesional";
            }

            // 2. IMPORTANTE: Agregamos el prefijo /usuarios para que el API Gateway (8080) 
            // reconozca la ruta y la derive al microservicio correcto.
            String linkConfirmacion = String.format("%s/usuarios/api/%s/confirmar?token=%s", appUrl, endpoint, token);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Personalizar el contenido según el tipo de usuario
            String htmlMsg;
            if (esProfesional) {
                htmlMsg = generarEmailProfesional(usuario, linkConfirmacion);
            } else if (esAdmin) {
                htmlMsg = generarEmailAdministrador(usuario, linkConfirmacion);
            } else {
                htmlMsg = generarEmailPaciente(usuario, linkConfirmacion);
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

    private String generarEmailProfesional(Usuario usuario, String linkConfirmacion) {
        return String.format(
            "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #3498db; max-width: 600px; margin: auto; border-radius: 8px;'>" +
            "<div style='text-align: center; margin-bottom: 20px;'>" +
            "  <h2 style='color: #2c3e50;'>Bienvenido, Profesional</h2>" +
            "</div>" +
            "<p>Hola <strong>%s</strong>,</p>" +
            "<p>¡Gracias por registrarte en el portal profesional de la <strong>Clínica UTN</strong>!</p>" +
            "<p>Para activar tu cuenta, por favor confirmá tu identidad haciendo clic en el siguiente botón:</p>" +
            "<div style='text-align: center; margin: 30px 0;'>" +
            "  <a href='%s' style='background-color: #3498db; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Confirmar mi Email</a>" +
            "</div>" +
            "<div style='background-color: #ecf0f1; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
            "  <p style='margin: 0; font-size: 0.9em; color: #555;'><strong>⏳ Próximo paso:</strong> Una vez confirmado, tu solicitud será revisada por nuestro equipo de Administración. Te avisaremos por correo cuando esté aprobado.</p>" +
            "</div>" +
            "<p style='font-size: 0.9em; color: #555;'>Si el botón no funciona, copía y pegá este link:</p>" +
            "<p style='word-break: break-all; color: #3498db; font-size: 0.85em;'>%s</p>" +
            "<hr style='border: 0; border-top: 1px solid #ecf0f1; margin-top: 30px;'>" +
            "<p style='font-size: 0.8em; color: #777; text-align: center;'>Este es un correo automático. No respondas a este mensaje.</p>" +
            "</div>",
            usuario.getNombre(), linkConfirmacion, linkConfirmacion
        );
    }

    private String generarEmailAdministrador(Usuario usuario, String linkConfirmacion) {
        return String.format(
            "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e74c3c; max-width: 600px; margin: auto; border-radius: 8px;'>" +
            "<div style='text-align: center; margin-bottom: 20px;'>" +
            "  <h2 style='color: #c0392b;'>Panel de Administrador</h2>" +
            "</div>" +
            "<p>Hola <strong>%s</strong>,</p>" +
            "<p>Tu cuenta de administrador ha sido creada en la <strong>Clínica UTN</strong>.</p>" +
            "<p>Para acceder al panel administrativo, por favor confirmá tu cuenta:</p>" +
            "<div style='text-align: center; margin: 30px 0;'>" +
            "  <a href='%s' style='background-color: #e74c3c; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Activar Cuenta Admin</a>" +
            "</div>" +
            "<p style='font-size: 0.9em; color: #555;'>Si el botón no funciona, copía y pegá este link:</p>" +
            "<p style='word-break: break-all; color: #3498db; font-size: 0.85em;'>%s</p>" +
            "<hr style='border: 0; border-top: 1px solid #ecf0f1; margin-top: 30px;'>" +
            "<p style='font-size: 0.8em; color: #777; text-align: center;'>Este es un correo automático. No respondas a este mensaje.</p>" +
            "</div>",
            usuario.getNombre(), linkConfirmacion, linkConfirmacion
        );
    }

    private String generarEmailPaciente(Usuario usuario, String linkConfirmacion) {
        return String.format(
            "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #27ae60; max-width: 600px; margin: auto; border-radius: 8px;'>" +
            "<h2 style='color: #27ae60;'>Bienvenido, Paciente</h2>" +
            "<p>¡Hola <strong>%s</strong>!</p>" +
            "<p>Gracias por registrarte en el sistema de la <strong>Clínica UTN</strong>.</p>" +
            "<p>Para activar tu cuenta y acceder al portal de pacientes, confirmá tu email haciendo clic aquí:</p>" +
            "<div style='text-align: center; margin: 30px 0;'>" +
            "  <a href='%s' style='background-color: #27ae60; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Confirmar mi Cuenta</a>" +
            "</div>" +
            "<p style='font-size: 0.9em; color: #555;'>Si el botón no funciona, copía y pegá este link:</p>" +
            "<p style='word-break: break-all; color: #3498db; font-size: 0.85em;'>%s</p>" +
            "<hr style='border: 0; border-top: 1px solid #ecf0f1; margin-top: 30px;'>" +
            "<p style='font-size: 0.8em; color: #777; text-align: center;'>Este es un correo automático del Sistema de Gestión de Historial Clínico.</p>" +
            "</div>",
            usuario.getNombre(), linkConfirmacion, linkConfirmacion
        );
    }
}