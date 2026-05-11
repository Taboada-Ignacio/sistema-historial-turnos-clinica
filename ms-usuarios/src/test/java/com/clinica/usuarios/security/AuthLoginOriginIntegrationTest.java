package com.clinica.usuarios.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Validación de Origin/Referer en login cuando {@code app.allowed-origins} está configurado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.allowed-origins=http://allowed.test")
class AuthLoginOriginIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Origin distinto al permitido → 403 INVALID_ORIGIN")
    void login_wrongOrigin_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Origin", "http://evil.test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@test.com\",\"password\":\"secret\",\"portal\":\"paciente\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("INVALID_ORIGIN"));
    }

    @Test
    @DisplayName("Origin permitido llega a autenticación (401 por credenciales, no 403 origen)")
    void login_allowedOrigin_badCredentials_stillUnauthorizedNotForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Origin", "http://allowed.test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@test.com\",\"password\":\"bad\",\"portal\":\"paciente\"}"))
                .andExpect(status().isUnauthorized());
    }
}
