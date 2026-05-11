package com.clinica.usuarios.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integración ligera: cadena de seguridad + validaciones públicas de login y refresh.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndAuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("GET maestro público sin JWT → 200")
    void getProvincias_withoutAuth_isOk() throws Exception {
        mockMvc.perform(get("/api/provincias"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET recurso protegido sin JWT → 401")
    void getPacientes_withoutAuth_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("JWT inválido en recurso protegido → 401 con código TOKEN_INVALID")
    void getPacientes_withGarbageBearer_returnsUnauthorizedWithCode() throws Exception {
        mockMvc.perform(get("/api/pacientes")
                        .header("Authorization", "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));
    }

    @Test
    @DisplayName("Login sin portal → 400 PORTAL_REQUERIDO")
    void login_missingPortal_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@test.com\",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PORTAL_REQUERIDO"));
    }

    @Test
    @DisplayName("Login portal inválido → 400 PORTAL_INVALIDO")
    void login_invalidPortal_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@test.com\",\"password\":\"secret\",\"portal\":\"intruso\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PORTAL_INVALIDO"));
    }

    @Test
    @DisplayName("Refresh sin cookie → 401 REFRESH_TOKEN_MISSING")
    void refresh_withoutCookie_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_MISSING"));
    }

    @Test
    @DisplayName("Credenciales inexistentes → 401 (sin validación de origen cuando allowed-origins vacío)")
    void login_validPortal_badCredentials_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"noexiste@test.com\",\"password\":\"wrong\",\"portal\":\"paciente\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Autenticación fallida"));
    }
}
