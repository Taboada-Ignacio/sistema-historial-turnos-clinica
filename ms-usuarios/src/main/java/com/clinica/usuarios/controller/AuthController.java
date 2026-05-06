package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.AuthRequestDTO;
import com.clinica.usuarios.dto.request.CambiarPasswordConTokenDTO;
import com.clinica.usuarios.dto.request.CambiarPasswordRequestDTO;
import com.clinica.usuarios.dto.request.SolicitarCambioPasswordDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.model.Usuario;
import com.clinica.usuarios.model.VerificationToken;
import com.clinica.usuarios.repository.VerificationTokenRepository;
import com.clinica.usuarios.repository.UsuarioRepository;
import com.clinica.usuarios.security.JwtUtil;
import com.clinica.usuarios.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.url}")
    private String appUrl;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO request) {
        try {
            // 1. Spring Security valida email, contraseña y el campo 'enabled' (Estado ACTIVO)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // 2. Si las credenciales son correctas y el usuario está ACTIVO, cargamos los datos
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

            // 3. Generamos el token JWT
            final String jwt = jwtUtil.generateToken(userDetails);

            // 4. Lo devolvemos en formato JSON
            return ResponseEntity.ok(Collections.singletonMap("token", jwt));

        } catch (DisabledException e) {
            // Este catch se dispara cuando enabled = false en UserDetailsServiceImpl
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Cuenta pendiente de activación", 
                                "message", "Por favor, confirmá tu email para poder ingresar al sistema."));

        } catch (BadCredentialsException e) {
            // Credenciales incorrectas
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticación fallida", 
                                "message", "Email o contraseña incorrectos."));

        } catch (Exception e) {
            // Error genérico para no romper el flujo
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno", 
                                "message", "Ocurrió un problema procesando tu solicitud."));
        }
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@Valid @RequestBody CambiarPasswordRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPasswordActual())
            );

            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cuenta con ese email."));

            if (passwordEncoder.matches(request.getPasswordNueva(), usuario.getPassword())) {
                throw new ReglaDeNegocioException("La nueva contraseña no puede ser igual a la actual.");
            }

            usuario.setPassword(passwordEncoder.encode(request.getPasswordNueva()));
            usuarioRepository.save(usuario);

            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticación fallida",
                            "message", "Email o contraseña actual incorrectos."));
        }
    }

    @PostMapping("/solicitar-cambio-password/paciente")
    public ResponseEntity<?> solicitarCambioPasswordPaciente(@Valid @RequestBody SolicitarCambioPasswordDTO request) {
        return solicitarCambioPasswordPorTipo(request.getEmail(), "ROLE_PACIENTE", "paciente");
    }

    @PostMapping("/solicitar-cambio-password/profesional")
    public ResponseEntity<?> solicitarCambioPasswordProfesional(@Valid @RequestBody SolicitarCambioPasswordDTO request) {
        return solicitarCambioPasswordPorTipo(request.getEmail(), "ROLE_PROFESIONAL", "profesional");
    }

    @GetMapping("/confirmar-cambio-password")
    public RedirectView confirmarCambioPassword(@RequestParam("token") String token, @RequestParam("tipo") String tipo) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de recuperación inválido."));

        if (verificationToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new ReglaDeNegocioException("El enlace de recuperación ha expirado.");
        }

        Usuario usuario = verificationToken.getUsuario();
        String roleEsperado = "profesional".equalsIgnoreCase(tipo) ? "ROLE_PROFESIONAL" : "ROLE_PACIENTE";
        validarRolUsuario(usuario, roleEsperado);

        String rutaFrontend = "profesional".equalsIgnoreCase(tipo)
                ? "/cambiar-password/profesional"
                : "/cambiar-password/paciente";

        return new RedirectView(frontendUrl + rutaFrontend + "?token=" + token);
    }

    @PostMapping("/cambiar-password-con-token/paciente")
    public ResponseEntity<?> cambiarPasswordConTokenPaciente(@Valid @RequestBody CambiarPasswordConTokenDTO request) {
        return cambiarPasswordConTokenPorTipo(request, "ROLE_PACIENTE");
    }

    @PostMapping("/cambiar-password-con-token/profesional")
    public ResponseEntity<?> cambiarPasswordConTokenProfesional(@Valid @RequestBody CambiarPasswordConTokenDTO request) {
        return cambiarPasswordConTokenPorTipo(request, "ROLE_PROFESIONAL");
    }

    private ResponseEntity<?> solicitarCambioPasswordPorTipo(String email, String roleEsperado, String tipoPortal) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cuenta con ese email."));

        validarRolUsuario(usuario, roleEsperado);
        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstadoActual().getNombre())) {
            throw new ReglaDeNegocioException("La cuenta debe estar activa para recuperar contraseña.");
        }

        verificationTokenRepository.deleteByUsuario(usuario);
        String token = UUID.randomUUID().toString();
        verificationTokenRepository.save(VerificationToken.builder()
                .token(token)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(30))
                .build());

        String linkConfirmacion = String.format("%s/usuarios/api/auth/confirmar-cambio-password?token=%s&tipo=%s", appUrl, token, tipoPortal);
        emailService.enviarEmailRecuperacionPassword(usuario, linkConfirmacion, tipoPortal);

        return ResponseEntity.ok(Map.of("message", "Te enviamos un correo para confirmar el cambio de contraseña."));
    }

    private ResponseEntity<?> cambiarPasswordConTokenPorTipo(CambiarPasswordConTokenDTO request, String roleEsperado) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de recuperación inválido."));

        if (verificationToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new ReglaDeNegocioException("El enlace de recuperación ha expirado.");
        }

        Usuario usuario = verificationToken.getUsuario();
        validarRolUsuario(usuario, roleEsperado);

        if (passwordEncoder.matches(request.getPasswordNueva(), usuario.getPassword())) {
            throw new ReglaDeNegocioException("La nueva contraseña no puede ser igual a la actual.");
        }

        usuario.setPassword(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);
        verificationTokenRepository.delete(verificationToken);

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
    }

    private void validarRolUsuario(Usuario usuario, String roleEsperado) {
        boolean cumple = usuario.getRoles().stream().anyMatch(r -> roleEsperado.equals(r.getDescripcion()));
        if (!cumple) {
            throw new ReglaDeNegocioException("La cuenta no corresponde al portal solicitado.");
        }
    }
}