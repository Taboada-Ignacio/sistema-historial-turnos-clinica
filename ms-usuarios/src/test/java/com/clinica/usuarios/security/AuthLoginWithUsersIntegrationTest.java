package com.clinica.usuarios.security;

import com.clinica.usuarios.model.*;
import com.clinica.usuarios.repository.*;
import com.clinica.usuarios.testsupport.TestEntidadFactory;
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
    private static final String EMAIL_PROF_DUAL = "prof.dual.auth@test.local";
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
    DireccionRepository direccionRepository;

    @Autowired
    ObraSocialRepository obraSocialRepository;

    @Autowired
    PacienteRepository pacienteRepository;

    @Autowired
    ProfesionalRepository profesionalRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    EspecialidadRepository especialidadRepository;

    @Autowired
    MembresiaRepository membresiaRepository;

    private Long idPacienteA;
    private Long idPacienteB;
    private Long idObraSocialSeed;
    private Long idLocalidadSeed;

    @BeforeEach
    void seedPacientesActivos() {
        Estado activo = estadoRepository.findByNombre("ACTIVO")
                .orElseGet(() -> estadoRepository.save(Estado.builder().nombre("ACTIVO").build()));

        Rol rolPaciente = rolRepository.findByDescripcion("ROLE_PACIENTE")
                .orElseGet(() -> rolRepository.save(Rol.builder().descripcion("ROLE_PACIENTE").build()));

        Provincia prov = provinciaRepository.save(Provincia.builder().nombre("ProvAuthTest").build());
        Localidad loc = localidadRepository.save(Localidad.builder().nombre("LocAuthTest").provincia(prov).build());
        Direccion dir = direccionRepository.save(Direccion.builder().nombre("Calle Test 1").localidad(loc).build());
        ObraSocial os = obraSocialRepository.save(ObraSocial.builder().descripcion("ObraAuthTest").build());
        idObraSocialSeed = os.getIdObraSocial();
        idLocalidadSeed = loc.getIdLocalidad();

        Paciente a = buildPaciente(EMAIL_A, 91234501, activo, rolPaciente, loc, dir, os);
        Paciente b = buildPaciente(EMAIL_B, 91234502, activo, rolPaciente, loc, dir, os);
        idPacienteA = pacienteRepository.save(a).getIdUsuario();
        idPacienteB = pacienteRepository.save(b).getIdUsuario();

        seedProfesionalDualRole(activo, prov, loc, dir);
    }

    private void seedProfesionalDualRole(Estado activo, Provincia prov, Localidad loc, Direccion dir) {
        Rol rolPaciente = rolRepository.findByDescripcion("ROLE_PACIENTE")
                .orElseGet(() -> rolRepository.save(Rol.builder().descripcion("ROLE_PACIENTE").build()));
        Rol rolProfesional = rolRepository.findByDescripcion("ROLE_PROFESIONAL")
                .orElseGet(() -> rolRepository.save(Rol.builder().descripcion("ROLE_PROFESIONAL").build()));
        Especialidad esp = especialidadRepository.save(
                Especialidad.builder().descripcion("MEDICINA GENERAL").build());
        Membresia membresia = membresiaRepository.findByNombre("SIN_VERIFICAR")
                .orElseGet(() -> membresiaRepository.save(Membresia.builder().nombre("SIN_VERIFICAR").build()));

        Usuario usuarioProf = usuarioRepository.save(TestEntidadFactory.nuevoUsuario(
                EMAIL_PROF_DUAL, passwordEncoder.encode(PASSWORD), activo, Set.of(rolPaciente, rolProfesional)));
        profesionalRepository.save(TestEntidadFactory.profesional(
                usuarioProf, "Pro", "Dual", 91234999, "+5491199999999",
                LocalDate.of(1985, 3, 10), Sexo.MASCULINO, dir, "MAT-DUAL-999", esp, membresia));
    }

    private Paciente buildPaciente(
            String email,
            int dni,
            Estado estado,
            Rol rol,
            Localidad loc,
            Direccion dir,
            ObraSocial os) {
        Usuario usuario = usuarioRepository.save(
                TestEntidadFactory.nuevoUsuario(email, passwordEncoder.encode(PASSWORD), estado, Set.of(rol)));
        return TestEntidadFactory.paciente(
                usuario, "Nombre", "Apellido", dni, "+5491122334455",
                LocalDate.of(1991, 6, 15), Sexo.MASCULINO, dir, os);
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

    @Test
    @DisplayName("JWT paciente → GET /api/pacientes/me devuelve su perfil")
    void login_then_getMe_returnsOwnProfile() throws Exception {
        String token = extractAccessToken(login(EMAIL_A));

        mockMvc.perform(get("/api/pacientes/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL_A))
                .andExpect(jsonPath("$.idUsuario").value(idPacienteA.intValue()))
                .andExpect(jsonPath("$.perfilEditable").value(true))
                .andExpect(jsonPath("$.tipoCuenta").value("PACIENTE"));
    }

    @Test
    @DisplayName("Profesional con ROLE_PACIENTE → login portal paciente → GET /me UI limitada")
    void profesionalDual_loginPacientePortal_meLimited() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildLoginJson(EMAIL_PROF_DUAL, PASSWORD, "paciente")))
                .andExpect(status().isOk())
                .andReturn();
        String token = extractAccessToken(loginResult);

        mockMvc.perform(get("/api/pacientes/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL_PROF_DUAL))
                .andExpect(jsonPath("$.perfilEditable").value(false))
                .andExpect(jsonPath("$.tipoCuenta").value("PROFESIONAL_EN_PORTAL_PACIENTE"));
    }

    @Test
    @DisplayName("Profesional dual → PUT /api/pacientes/{id} → 400 PERFIL_PACIENTE_NO_DISPONIBLE")
    void profesionalDual_putPacientePerfil_rejected() throws Exception {
        Profesional prof = profesionalRepository.findByUsuario_Email(EMAIL_PROF_DUAL).orElseThrow();
        String token = extractAccessToken(mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildLoginJson(EMAIL_PROF_DUAL, PASSWORD, "paciente")))
                .andExpect(status().isOk())
                .andReturn());

        String body = """
                {"nombre":"Pro","apellido":"Dual","dni":91234999,"email":"%s","telefono":"+5491199999999",
                "fechaNacimiento":"1985-03-10","sexo":"MASCULINO","idObraSocial":%d,"idLocalidad":%d,"direccion":"Calle Test 1"}
                """.formatted(EMAIL_PROF_DUAL, idObraSocialSeed, idLocalidadSeed);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/api/pacientes/" + prof.getIdUsuario())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PERFIL_PACIENTE_NO_DISPONIBLE"));
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
