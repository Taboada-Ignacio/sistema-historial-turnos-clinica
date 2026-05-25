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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfesionalPacientesBusquedaIntegrationTest {

    private static final String EMAIL_PROF = "prof.busqueda.pac@test.local";
    private static final String EMAIL_PACIENTE_ZONA = "paciente.zona.busq@test.local";
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
    UsuarioRepository usuarioRepository;

    @Autowired
    ProfesionalRepository profesionalRepository;

    @Autowired
    AdministradorRepository administradorRepository;

    @Autowired
    EspecialidadRepository especialidadRepository;

    @Autowired
    MembresiaRepository membresiaRepository;

    @BeforeEach
    void seed() {
        Estado activo = estadoRepository.findByNombre("ACTIVO")
                .orElseGet(() -> estadoRepository.save(Estado.builder().nombre("ACTIVO").build()));

        Rol rolPaciente = rolRepository.findByDescripcion("ROLE_PACIENTE")
                .orElseGet(() -> rolRepository.save(Rol.builder().descripcion("ROLE_PACIENTE").build()));
        Rol rolProfesional = rolRepository.findByDescripcion("ROLE_PROFESIONAL")
                .orElseGet(() -> rolRepository.save(Rol.builder().descripcion("ROLE_PROFESIONAL").build()));
        Rol rolAdmin = rolRepository.findByDescripcion("ROLE_ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(Rol.builder().descripcion("ROLE_ADMINISTRADOR").build()));

        Provincia prov = provinciaRepository.save(Provincia.builder().nombre("ProvBusqProf").build());
        Localidad loc = localidadRepository.save(Localidad.builder().nombre("LocBusqProf").provincia(prov).build());
        Direccion dir = direccionRepository.save(Direccion.builder().nombre("Calle Busq 1").localidad(loc).build());
        ObraSocial os = obraSocialRepository.save(ObraSocial.builder().descripcion("ObraBusqProf").build());

        Usuario usuarioPaciente = usuarioRepository.save(TestEntidadFactory.nuevoUsuario(
                EMAIL_PACIENTE_ZONA, passwordEncoder.encode(PASSWORD), activo, Set.of(rolPaciente)));
        pacienteRepository.save(TestEntidadFactory.paciente(
                usuarioPaciente, "Carlos", "Rodriguez", 30111222, "+5491100000001",
                LocalDate.of(1990, 1, 1), Sexo.MASCULINO, dir, os));

        Especialidad esp = especialidadRepository.save(
                Especialidad.builder().descripcion("CLINICA MEDICA").build());
        Membresia inactiva = membresiaRepository.findByNombre("INACTIVA")
                .orElseGet(() -> membresiaRepository.save(Membresia.builder().nombre("INACTIVA").build()));

        Usuario usuarioProf = usuarioRepository.save(TestEntidadFactory.nuevoUsuario(
                EMAIL_PROF, passwordEncoder.encode(PASSWORD), activo, Set.of(rolProfesional)));
        profesionalRepository.save(TestEntidadFactory.profesional(
                usuarioProf, "Ana", "Medica", 28999888, "+5491100000002",
                LocalDate.of(1980, 5, 5), Sexo.MASCULINO, dir, "MAT-BUSQ-001", esp, inactiva));

        Usuario usuarioAdmin = usuarioRepository.save(TestEntidadFactory.nuevoUsuario(
                "admin.zona.busq@test.local", passwordEncoder.encode(PASSWORD), activo, Set.of(rolAdmin)));
        administradorRepository.save(TestEntidadFactory.administrador(
                usuarioAdmin, "Luis", "ZonaAdmin", 27777111, "+5491100000003",
                LocalDate.of(1975, 3, 10), Sexo.MASCULINO, dir));

        Estado bloqueado = estadoRepository.findByNombre("BLOQUEADO")
                .orElseGet(() -> estadoRepository.save(Estado.builder().nombre("BLOQUEADO").build()));

        Usuario usuarioBloqueado = usuarioRepository.save(TestEntidadFactory.nuevoUsuario(
                "paciente.bloqueado.busq@test.local", passwordEncoder.encode(PASSWORD), bloqueado, Set.of(rolPaciente)));
        pacienteRepository.save(TestEntidadFactory.paciente(
                usuarioBloqueado, "Oculto", "BloqueadoZona", 30999999, "+5491100000099",
                LocalDate.of(1988, 8, 8), Sexo.FEMENINO, dir, os));
    }

    private Long idPacienteEnZona() {
        return usuarioRepository.findByEmail(EMAIL_PACIENTE_ZONA).orElseThrow().getIdUsuario();
    }

    @Test
    @DisplayName("Profesional verificado → buscar por apellido en su zona → encuentra paciente")
    void buscarPorApellido_enMismaLocalidad_returnsPaciente() throws Exception {
        String token = loginProfesional();

        mockMvc.perform(get("/api/profesionales/me/pacientes/buscar")
                        .param("q", "Rodriguez")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.personas[0].apellido").value("Rodriguez"))
                .andExpect(jsonPath("$.personas[0].tipoCuenta").value("PACIENTE"));
    }

    @Test
    @DisplayName("Profesional → buscar por DNI en su zona → encuentra paciente")
    void buscarPorDni_enMismaLocalidad_returnsPaciente() throws Exception {
        String token = loginProfesional();

        mockMvc.perform(get("/api/profesionales/me/pacientes/buscar")
                        .param("q", "30111222")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.personas[0].dni").value(30111222));
    }

    @Test
    @DisplayName("Profesional → buscar por apellido de admin en su zona → encuentra administrador")
    void buscarPorApellido_adminEnMismaLocalidad_returnsAdministrador() throws Exception {
        String token = loginProfesional();

        mockMvc.perform(get("/api/profesionales/me/pacientes/buscar")
                        .param("q", "ZonaAdmin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.personas[0].tipoCuenta").value("ADMINISTRADOR"));
    }

    @Test
    @DisplayName("Profesional → búsqueda general con provincia y localidad → encuentra paciente")
    void buscarGeneral_conProvinciaYLocalidad_returnsPaciente() throws Exception {
        String token = loginProfesional();
        Long idProvincia = provinciaRepository.findAll().stream()
                .filter(p -> "ProvBusqProf".equals(p.getNombre()))
                .findFirst()
                .orElseThrow()
                .getIdProvincia();
        Long idLocalidad = localidadRepository.findAll().stream()
                .filter(l -> "LocBusqProf".equals(l.getNombre()))
                .findFirst()
                .orElseThrow()
                .getIdLocalidad();

        mockMvc.perform(get("/api/profesionales/me/pacientes/buscar-general")
                        .param("q", "Rodriguez")
                        .param("idProvincia", String.valueOf(idProvincia))
                        .param("idLocalidad", String.valueOf(idLocalidad))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.personas[0].apellido").value("Rodriguez"));
    }

    @Test
    @DisplayName("Profesional → detalle de paciente en su zona → datos de contacto y dirección")
    void detallePacienteEnZona_returnsOk() throws Exception {
        String token = loginProfesional();
        Long id = idPacienteEnZona();

        mockMvc.perform(get("/api/profesionales/me/pacientes/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos"))
                .andExpect(jsonPath("$.apellido").value("Rodriguez"))
                .andExpect(jsonPath("$.dni").value(30111222))
                .andExpect(jsonPath("$.telefono").value("+5491100000001"))
                .andExpect(jsonPath("$.nombreLocalidad").value("LocBusqProf"))
                .andExpect(jsonPath("$.direccion").value("Calle Busq 1"))
                .andExpect(jsonPath("$.tipoCuenta").value("PACIENTE"));
    }

    @Test
    @DisplayName("Profesional → paciente BLOQUEADO en la zona no aparece en búsqueda")
    void buscarPacienteBloqueado_enMismaLocalidad_excluded() throws Exception {
        String token = loginProfesional();

        mockMvc.perform(get("/api/profesionales/me/pacientes/buscar")
                        .param("q", "BloqueadoZona")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("Profesional → detalle de paciente BLOQUEADO → 404")
    void detallePacienteBloqueado_returnsNotFound() throws Exception {
        String token = loginProfesional();
        Long id = usuarioRepository.findByEmail("paciente.bloqueado.busq@test.local").orElseThrow().getIdUsuario();

        mockMvc.perform(get("/api/profesionales/me/pacientes/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Profesional sin texto → 400")
    void buscarSinTexto_returnsBadRequest() throws Exception {
        String token = loginProfesional();

        mockMvc.perform(get("/api/profesionales/me/pacientes/buscar")
                        .param("q", "   ")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Paciente sin rol profesional → 403")
    void buscarComoPaciente_returnsForbidden() throws Exception {
        String token = loginPaciente();

        mockMvc.perform(get("/api/profesionales/me/pacientes/buscar")
                        .param("q", "Rodriguez")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String loginProfesional() throws Exception {
        return extractToken(login(EMAIL_PROF, "profesional"));
    }

    private String loginPaciente() throws Exception {
        return extractToken(login(EMAIL_PACIENTE_ZONA, "paciente"));
    }

    private MvcResult login(String email, String portal) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","portal":"%s"}
                                """.formatted(email, PASSWORD, portal)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String extractToken(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}
