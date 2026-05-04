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
            
            boolean esAdmin = usuario.getRoles().stream()
                    .anyMatch(r -> r.getDescripcion().equals("ROLE_ADMINISTRADOR"));
            boolean esProfesional = usuario.getRoles().stream()
                    .anyMatch(r -> r.getDescripcion().equals("ROLE_PROFESIONAL"));

            if (esAdmin) {
                endpoint = "administradores";
            } else if (esProfesional) {
                endpoint = "profesionales";
            }

            // 2. IMPORTANTE: Agregamos el prefijo /usuarios para que el API Gateway (8080) 
            // reconozca la ruta y la derive al microservicio correcto.
            String linkConfirmacion = String.format("%s/usuarios/api/%s/confirmar?token=%s", appUrl, endpoint, token);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = String.format(
                "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; max-width: 600px; margin: auto;'>" +
                "<h2 style='color: #2c3e50;'>¡Hola, %s!</h2>" +
                "<p>Gracias por registrarte en el sistema de la <strong>Clínica UTN</strong>.</p>" +
                "<p>Para activar tu cuenta y acceder al portal, por favor hacé clic en el siguiente botón:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "  <a href='%s' style='background-color: #e74c3c; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Activar mi Cuenta</a>" +
                "</div>" +
                "<p style='font-size: 0.9em; color: #555;'>Si el botón no funciona, podés copiar y pegar este link en tu navegador:</p>" +
                "<p style='word-break: break-all; color: #3498db; font-size: 0.85em;'>%s</p>" +
                "<hr style='border: 0; border-top: 1px solid #eee; margin-top: 30px;'>" +
                "<p style='font-size: 0.8em; color: #777; text-align: center;'>Este es un correo automático del Sistema de Gestión de Historial Clínico.</p>" +
                "</div>",
                usuario.getNombre(), linkConfirmacion, linkConfirmacion
            );

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
}