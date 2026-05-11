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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
    private final com.clinica.usuarios.service.impl.RefreshTokenService refreshTokenService;

    @Value("${app.url}")
    private String appUrl;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;
    
    @Value("${jwt.refreshExpiration:604800000}")
    private long refreshExpirationMs;
    
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;
    
    @Value("${app.allowed-origins:}")
    private String allowedOriginsCsv;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO request, HttpServletResponse response, HttpServletRequest requestHttp) {
        try {
            if (request.getPortal() == null || request.getPortal().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Solicitud inválida",
                                "message", "Debe indicar el portal: paciente, profesional o admin.",
                                "code", "PORTAL_REQUERIDO"));
            }

            String portalNormalizado = request.getPortal().trim().toLowerCase();
            if (!List.of("paciente", "profesional", "admin").contains(portalNormalizado)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Solicitud inválida",
                                "message", "El portal debe ser: paciente, profesional o admin.",
                                "code", "PORTAL_INVALIDO"));
            }

            // Origin/Referer validation to mitigate CSRF for browser-based login when cookies or sensitive flows are used.
            String allowed = allowedOriginsCsv == null ? "" : allowedOriginsCsv.trim();
            if (!allowed.isEmpty()) {
                String origin = requestHttp.getHeader("Origin");
                boolean originOk = false;
                if (origin != null && !origin.isBlank()) {
                    for (String o : allowed.split(",")) {
                        if (origin.equalsIgnoreCase(o.trim())) {
                            originOk = true;
                            break;
                        }
                    }
                } else {
                    String referer = requestHttp.getHeader("Referer");
                    if (referer != null && !referer.isBlank()) {
                        for (String o : allowed.split(",")) {
                            if (referer.startsWith(o.trim())) {
                                originOk = true;
                                break;
                            }
                        }
                    }
                }
                if (!originOk) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Origen no permitido", "code", "INVALID_ORIGIN"));
                }
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

            if (!tieneAccesoAlPortal(userDetails, portalNormalizado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "error", "Acceso denegado",
                                "message", "Esta cuenta no tiene acceso a este portal.",
                                "code", "PORTAL_NO_PERMITIDO"));
            }

            final String jwt = jwtUtil.generateToken(userDetails);

            // Create refresh token (rotating, stored in DB) and set as HttpOnly cookie
            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            com.clinica.usuarios.model.RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .maxAge(refreshExpirationMs / 1000)
                    .sameSite("Lax")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

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
            // Revoke refresh tokens for the user after password change
            refreshTokenService.revokeAllForUser(usuario);

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

    @PostMapping("/solicitar-cambio-password/admin")
    public ResponseEntity<?> solicitarCambioPasswordAdmin(@Valid @RequestBody SolicitarCambioPasswordDTO request) {
        return solicitarCambioPasswordPorTipo(request.getEmail(), "ROLE_ADMINISTRADOR", "admin");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletResponse response,
            HttpServletRequest request) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token no proporcionado", "code", "REFRESH_TOKEN_MISSING"));
        }
        try {
            // Origin/Referer validation to mitigate CSRF when using cookies for refresh
            String allowed = allowedOriginsCsv == null ? "" : allowedOriginsCsv.trim();
            if (!allowed.isEmpty()) {
                String origin = request.getHeader("Origin");
                boolean originOk = false;
                if (origin != null && !origin.isBlank()) {
                    for (String o : allowed.split(",")) {
                        if (origin.equalsIgnoreCase(o.trim())) {
                            originOk = true;
                            break;
                        }
                    }
                } else {
                    // Fallback to Referer check (less strict): compare host
                    String referer = request.getHeader("Referer");
                    if (referer != null && !referer.isBlank()) {
                        for (String o : allowed.split(",")) {
                            if (referer.startsWith(o.trim())) {
                                originOk = true;
                                break;
                            }
                        }
                    }
                }
                if (!originOk) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Origen no permitido", "code", "INVALID_ORIGIN"));
                }
            }
            com.clinica.usuarios.model.RefreshToken newRefresh = refreshTokenService.verifyAndRotate(refreshTokenCookie);
            Usuario usuario = newRefresh.getUsuario();
            final UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
            final String newJwt = jwtUtil.generateToken(userDetails);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefresh.getToken())
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .maxAge(refreshExpirationMs / 1000)
                    .sameSite("Lax")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(Collections.singletonMap("token", newJwt));
        } catch (RecursoNoEncontradoException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token inválido o expirado", "code", "REFRESH_TOKEN_INVALID"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No se pudo procesar refresh token", "code", "REFRESH_TOKEN_INVALID"));
        }
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
        String tipoNormalizado = tipo == null ? "" : tipo.trim().toLowerCase();
        String roleEsperado = switch (tipoNormalizado) {
            case "profesional" -> "ROLE_PROFESIONAL";
            case "admin" -> "ROLE_ADMINISTRADOR";
            default -> "ROLE_PACIENTE";
        };
        validarRolUsuario(usuario, roleEsperado);

        String rutaFrontend = switch (tipoNormalizado) {
            case "profesional" -> "/cambiar-password/profesional";
            case "admin" -> "/cambiar-password/admin";
            default -> "/cambiar-password/paciente";
        };

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

    @PostMapping("/cambiar-password-con-token/admin")
    public ResponseEntity<?> cambiarPasswordConTokenAdmin(@Valid @RequestBody CambiarPasswordConTokenDTO request) {
        return cambiarPasswordConTokenPorTipo(request, "ROLE_ADMINISTRADOR");
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
        // Revoke refresh tokens after a password reset via token
        refreshTokenService.revokeAllForUser(usuario);

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
    }

    private void validarRolUsuario(Usuario usuario, String roleEsperado) {
        boolean cumple = usuario.getRoles().stream().anyMatch(r -> roleEsperado.equals(r.getDescripcion()));
        if (!cumple) {
            throw new ReglaDeNegocioException("La cuenta no corresponde al portal solicitado.");
        }
    }

    private boolean tieneAccesoAlPortal(UserDetails userDetails, String portal) {
        String requiredAuthority = switch (portal) {
            case "paciente" -> "ROLE_PACIENTE";
            case "profesional" -> "ROLE_PROFESIONAL";
            case "admin" -> "ROLE_ADMINISTRADOR";
            default -> null;
        };
        if (requiredAuthority == null) {
            return false;
        }
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(requiredAuthority::equals);
    }
}