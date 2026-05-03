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

    @Value("${app.url}")
    private String appUrl;

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    @Async // Crucial: Envía el mail en un hilo separado para no trabar el registro
    public void enviarEmailConfirmacion(Usuario usuario, String token) {
        try {
            // Determinamos el endpoint según el tipo de usuario para el link
            String endpoint = usuario.getRoles().stream()
                    .anyMatch(r -> r.getDescripcion().equals("ROLE_PACIENTE")) ? "pacientes" : "profesionales";
            
            // Si es administrador, podrías tener otro endpoint o manejarlo general
            if(usuario.getRoles().stream().anyMatch(r -> r.getDescripcion().equals("ROLE_ADMINISTRADOR"))) {
                endpoint = "administradores";
            }

            String linkConfirmacion = String.format("%s/api/%s/confirmar?token=%s", appUrl, endpoint, token);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = String.format(
                "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd;'>" +
                "<h2>¡Hola, %s!</h2>" +
                "<p>Gracias por registrarte en el sistema de la <strong>Clínica UTN</strong>.</p>" +
                "<p>Para activar tu cuenta y acceder a todas las funcionalidades, por favor hacé clic en el siguiente botón:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "  <a href='%s' style='background-color: #007bff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Confirmar mi Cuenta</a>" +
                "</div>" +
                "<p>Si el botón no funciona, podés copiar y pegar este link en tu navegador:</p>" +
                "<p style='word-break: break-all; color: #007bff;'>%s</p>" +
                "<hr style='border: 0; border-top: 1px solid #eee;'>" +
                "<p style='font-size: 0.8em; color: #777;'>Este es un correo automático, por favor no lo respondas.</p>" +
                "</div>",
                usuario.getNombre(), linkConfirmacion, linkConfirmacion
            );

            helper.setText(htmlMsg, true); // 'true' indica que es HTML
            helper.setTo(usuario.getEmail());
            helper.setSubject("Activá tu cuenta - Clínica UTN");
            helper.setFrom(sender);

            mailSender.send(mimeMessage);
            log.info("Email de confirmación enviado exitosamente a: {}", usuario.getEmail());

        } catch (MessagingException e) {
            log.error("Error al enviar el email a {}: {}", usuario.getEmail(), e.getMessage());
        }
    }
}