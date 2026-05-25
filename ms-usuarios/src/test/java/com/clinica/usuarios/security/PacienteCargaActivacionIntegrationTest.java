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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PacienteCargaActivacionIntegrationTest {

    private static final String EMAIL_BLOQUEADO = "bloqueado.carga@test.local";
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

    @Autowired
    TransactionTemplate transactionTemplate;

    private String emailProf;
    private String emailPacCarga;
    private Long idLocalidad;
    private Long idObraSocial;

    @BeforeEach
    void seed() {
        String uniq = UUID.randomUUID().toString().substring(0, 8);
        emailProf = "prof.carga.pac." + uniq + "@test.local";
        emailPacCarga = "paciente.carga." + uniq + "@test.local";
        int dniProf = 28_111_222 + uniq.hashCode() % 70_000;
        transactionTemplate.executeWithoutResult(status -> {
            estadoRepository.findByNombre("SIN_CONTRASENA")
                    .orElseGet(() -> estadoRepository.save(Estado.builder().nombre("SIN_CONTRASENA").build()));

            Estado activo = estadoRepository.findByNombre("ACTIVO")
                    .orElseGet(() -> estadoRepository.save(Estado.builder().nombre("ACTIVO").build()));

            Rol rolProf = rolRepository.findByDescripcion("ROLE_PROFESIONAL")
                    .orElseGet(() -> rolRepository.save(Rol.builder().descripcion("ROLE_PROFESIONAL").build()));
            rolRepository.findByDescripcion("ROLE_PACIENTE")
                    .orElseGet(() -> rolRepository.save(Rol.builder().descripcion("ROLE_PACIENTE").build()));

            Provincia prov = provinciaRepository.save(
                    Provincia.builder().nombre("ProvCargaPac-" + uniq).build());
            Localidad loc = localidadRepository.save(
                    Localidad.builder().nombre("LocCargaPac-" + uniq).provincia(prov).build());
            idLocalidad = loc.getIdLocalidad();
            Direccion dir = direccionRepository.save(
                    Direccion.builder().nombre("Calle Carga 1").localidad(loc).build());
            idObraSocial = obraSocialRepository.save(
                    ObraSocial.builder().descripcion("ObraCargaPac-" + uniq).build()).getIdObraSocial();

            Especialidad esp = especialidadRepository.save(
                    Especialidad.builder().descripcion("MEDICINA GENERAL " + uniq).build());
            Membresia inactiva = membresiaRepository.findByNombre("INACTIVA")
                    .orElseGet(() -> membresiaRepository.save(Membresia.builder().nombre("INACTIVA").build()));

            Usuario usuarioProf = usuarioRepository.save(TestEntidadFactory.nuevoUsuario(
                    emailProf, passwordEncoder.encode(PASSWORD), activo, Set.of(rolProf)));
            profesionalRepository.save(TestEntidadFactory.profesional(
                    usuarioProf, "Pro", "Carga", dniProf, "+5491100000100",
                    LocalDate.of(1985, 1, 1), Sexo.MASCULINO, dir, "MAT-CARGA-" + uniq, esp, inactiva));
        });
    }

    @Test
    @DisplayName("Profesional carga paciente → 201 y reenvío acceso OK")
    void cargarPaciente_yReenviarAcceso() throws Exception {
        String tokenProf = loginProfesional();
        SecurityContextHolder.clearContext();

        Map<String, Object> cargaBody = new LinkedHashMap<>();
        cargaBody.put("nombre", "Maria");
        cargaBody.put("apellido", "Cargada");
        cargaBody.put("dni", 31222333);
        cargaBody.put("email", emailPacCarga);
        cargaBody.put("telefono", "+5491100000200");
        cargaBody.put("fechaNacimiento", "1995-06-15");
        cargaBody.put("sexo", "FEMENINO");
        cargaBody.put("idLocalidad", idLocalidad);
        cargaBody.put("direccion", "Av. Carga 100");
        cargaBody.put("idObraSocial", idObraSocial);

        mockMvc.perform(post("/api/profesionales/me/pacientes")
                        .header("Authorization", "Bearer " + tokenProf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cargaBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").exists());

        mockMvc.perform(post("/api/auth/reenviar-acceso-paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\"}".formatted(emailPacCarga)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Paciente BLOQUEADO → solicitar cambio password → 400")
    void solicitarCambioPassword_bloqueado_returnsBadRequest() throws Exception {
        Estado bloqueado = estadoRepository.findByNombre("BLOQUEADO")
                .orElseGet(() -> estadoRepository.save(Estado.builder().nombre("BLOQUEADO").build()));
        Rol rolPac = rolRepository.findByDescripcion("ROLE_PACIENTE").orElseThrow();
        Direccion dir = direccionRepository.findAll().stream().findFirst().orElseThrow();
        ObraSocial os = obraSocialRepository.findById(idObraSocial).orElseThrow();

        Usuario usuarioBloqueado = usuarioRepository.save(TestEntidadFactory.nuevoUsuario(
                EMAIL_BLOQUEADO, passwordEncoder.encode(PASSWORD), bloqueado, Set.of(rolPac)));
        pacienteRepository.save(TestEntidadFactory.paciente(
                usuarioBloqueado, "Bloq", "User", 29999111, "+5491100000300",
                LocalDate.of(1990, 1, 1), Sexo.MASCULINO, dir, os));

        mockMvc.perform(post("/api/auth/solicitar-cambio-password/paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\"}".formatted(EMAIL_BLOQUEADO)))
                .andExpect(status().isBadRequest());
    }

    private String loginProfesional() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","portal":"profesional"}
                                """.formatted(emailProf, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}
