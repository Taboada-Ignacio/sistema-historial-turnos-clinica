package com.clinica.usuarios.security;

import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Login real contra usuarios persistidos: éxito, portal incorrecto y acceso con JWT a {@code /api/pacientes/{id}}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthLoginWithUsersIntegrationTest {

    private static final String EMAIL_A = "paciente.auth.a@test.local";
    private static final String EMAIL_B = "paciente.auth.b@test.local";
    private static final String PASSWORD = "TestPass123!";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EstadoRepository estadoRepository;

    @Autowired
    RolRepository rolRepository;

    @Autowired
    ProvinciaRepository provinciaRepository;

    @Autowired
    LocalidadRepository localidadRepository;

    @Autowired
    ObraSocialRepository obraSocialRepository;

    @Autowired
    PacienteRepository pacienteRepository;

    private Long idPacienteA;
    private Long idPacienteB;

    @BeforeEach
    void seedPacientesActivos() {
        Estado activo = estadoRepository.findByNombre("ACTIVO")
                .orElseGet(() -> estadoRepository.save(Estado.builder().nombre("ACTIVO").build()));

        Rol rolPaciente = rolRepository.findByDescripcion("ROLE_PACIENTE")
                .orElseGet(() -> rolRepository.save(Rol.builder().descripcion("ROLE_PACIENTE").build()));

        Provincia prov = provinciaRepository.save(Provincia.builder().nombre("ProvAuthTest").build());
        Localidad loc = localidadRepository.save(Localidad.builder().nombre("LocAuthTest").provincia(prov).build());
        ObraSocial os = obraSocialRepository.save(ObraSocial.builder().descripcion("ObraAuthTest").build());

        Paciente a = buildPaciente(EMAIL_A, 91234501, activo, rolPaciente, loc, os);
        Paciente b = buildPaciente(EMAIL_B, 91234502, activo, rolPaciente, loc, os);
        idPacienteA = pacienteRepository.save(a).getIdUsuario();
        idPacienteB = pacienteRepository.save(b).getIdUsuario();
    }

    private Paciente buildPaciente(
            String email,
            int dni,
            Estado estado,
            Rol rol,
            Localidad loc,
            ObraSocial os) {
        Paciente p = new Paciente();
        p.setEmail(email);
        p.setPassword(passwordEncoder.encode(PASSWORD));
        p.setNombre("Nombre");
        p.setApellido("Apellido");
        p.setDni(dni);
        p.setTelefono("+5491122334455");
        p.setFechaNacimiento(LocalDate.of(1991, 6, 15));
        p.setEstadoActual(estado);
        p.setRoles(Set.of(rol));
        p.setLocalidad(loc);
        p.setObraSocial(os);
        return p;
    }

    @Test
    @DisplayName("Login paciente válido → 200, JWT y cookie refreshToken")
    void login_success_returnsTokenAndRefreshCookie() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildLoginJson(EMAIL_A, PASSWORD, "paciente")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    @DisplayName("Paciente sin rol profesional → login portal profesional → 403 PORTAL_NO_PERMITIDO")
    void login_wrongPortal_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildLoginJson(EMAIL_A, PASSWORD, "profesional")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PORTAL_NO_PERMITIDO"));
    }

    @Test
    @DisplayName("JWT paciente puede ver su propio recurso GET /api/pacientes/{id}")
    void login_then_getOwnPaciente_returnsOk() throws Exception {
        String token = extractAccessToken(login(EMAIL_A));

        mockMvc.perform(get("/api/pacientes/" + idPacienteA)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL_A));
    }

    @Test
    @DisplayName("JWT paciente no puede ver otro paciente → 403")
    void login_then_getOtherPaciente_returnsForbidden() throws Exception {
        String token = extractAccessToken(login(EMAIL_A));

        mockMvc.perform(get("/api/pacientes/" + idPacienteB)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private MvcResult login(String email) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildLoginJson(email, PASSWORD, "paciente")))
                .andExpect(status().isOk())
                .andReturn();
    }

    private static String buildLoginJson(String email, String password, String portal) {
        return """
                {"email":"%s","password":"%s","portal":"%s"}
                """.formatted(email, password, portal);
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(root.hasNonNull("token")).isTrue();
        return root.get("token").asText();
    }
}
