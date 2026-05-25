package com.clinica.usuarios.config;

import com.clinica.usuarios.repository.EstadoRepository;
import com.clinica.usuarios.repository.MembresiaRepository;
import com.clinica.usuarios.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class StartupChecks implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupChecks.class);

    private final Environment env;
    private final DataSource dataSource;
    private final RolRepository rolRepository;
    private final EstadoRepository estadoRepository;
    private final MembresiaRepository membresiaRepository;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.allowed-origins:}")
    private String allowedOrigins;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${app.startup.strict:true}")
    private boolean startupStrict;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean prod = env.acceptsProfiles(Profiles.of("prod")) ||
                Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.equalsIgnoreCase("production"));

        if (prod && startupStrict) {
            log.info("Running strict startup checks for production profile");
            checkCookieSecure();
            checkAllowedOrigins();
            checkJwtSecret();
            checkDatabaseConnectivity();
            checkSeedData();
            log.info("Startup checks passed.");
        } else {
            log.info("Startup checks running in non-strict mode (prod:{} strict:{})", prod, startupStrict);
            // Run lightweight checks and log warnings
            try {
                checkJwtSecretWarn();
            } catch (Exception e) {
                log.warn("Startup warning: {}", e.getMessage());
            }
        }
    }

    private void checkCookieSecure() {
        if (!cookieSecure) {
            throw new IllegalStateException("APP_COOKIE_SECURE must be true in production");
        }
    }

    private void checkAllowedOrigins() {
        if (allowedOrigins == null || allowedOrigins.trim().isEmpty()) {
            throw new IllegalStateException("APP_ALLOWED_ORIGINS (app.allowed-origins) must be configured in production");
        }
        if (allowedOrigins.contains("localhost")) {
            throw new IllegalStateException("APP_ALLOWED_ORIGINS must not contain localhost in production");
        }
    }

    private void checkJwtSecret() {
        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be set and at least 32 characters long in production");
        }
        // Basic sanity: not the common placeholder value
        if ("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970".equals(jwtSecret)) {
            throw new IllegalStateException("JWT_SECRET appears to be the default placeholder; rotate it before production");
        }
    }

    private void checkJwtSecretWarn() {
        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET is missing or weak");
        }
    }

    private void checkDatabaseConnectivity() {
        try (Connection c = dataSource.getConnection()) {
            if (c == null || c.isClosed()) {
                throw new IllegalStateException("Database connection could not be established");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Database connectivity check failed: " + e.getMessage(), e);
        }
    }

    private void checkSeedData() {
        // Roles
        rolRepository.findByDescripcion("ROLE_ADMINISTRADOR")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMINISTRADOR not present in roles table"));
        rolRepository.findByDescripcion("ROLE_PROFESIONAL")
                .orElseThrow(() -> new IllegalStateException("ROLE_PROFESIONAL not present in roles table"));
        rolRepository.findByDescripcion("ROLE_PACIENTE")
                .orElseThrow(() -> new IllegalStateException("ROLE_PACIENTE not present in roles table"));

        // Estados
        estadoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new IllegalStateException("Estado 'PENDIENTE' missing"));
        estadoRepository.findByNombre("ACTIVO")
                .orElseThrow(() -> new IllegalStateException("Estado 'ACTIVO' missing"));
        estadoRepository.findByNombre("BLOQUEADO")
                .orElseThrow(() -> new IllegalStateException("Estado 'BLOQUEADO' missing"));
        estadoRepository.findByNombre("SIN_CONTRASENA")
                .orElseThrow(() -> new IllegalStateException("Estado 'SIN_CONTRASENA' missing"));

        // Membresias
        membresiaRepository.findByNombre("SIN_VERIFICAR")
                .orElseThrow(() -> new IllegalStateException("Membresia 'SIN_VERIFICAR' missing"));
        membresiaRepository.findByNombre("INACTIVA")
                .orElseThrow(() -> new IllegalStateException("Membresia 'INACTIVA' missing"));
    }
}

