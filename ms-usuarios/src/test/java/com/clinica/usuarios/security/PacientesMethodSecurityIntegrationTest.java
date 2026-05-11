package com.clinica.usuarios.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code @PreAuthorize} en listado de pacientes: solo administrador.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PacientesMethodSecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/pacientes con ROLE_ADMINISTRADOR → 200")
    @WithMockUser(roles = "ADMINISTRADOR")
    void listPacientes_asAdmin_returnsOk() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/pacientes con ROLE_PACIENTE → 403")
    @WithMockUser(roles = "PACIENTE")
    void listPacientes_asPaciente_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/pacientes sin autenticación → 401")
    void listPacientes_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isUnauthorized());
    }
}
