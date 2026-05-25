package com.clinica.usuarios.service;

import com.clinica.usuarios.dto.request.PacienteCargaProfesionalDTO;
import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.response.PacienteCargaProfesionalResponseDTO;
import com.clinica.usuarios.dto.request.ProfesionalUpdateDTO;
import com.clinica.usuarios.dto.request.RechazarProfesionalPendienteDTO;
import com.clinica.usuarios.dto.response.PersonaEnZonaBusquedaResponseDTO;
import com.clinica.usuarios.dto.response.PersonaEnZonaDetalleDTO;
import com.clinica.usuarios.dto.response.ProfesionalBusquedaResponseDTO;
import com.clinica.usuarios.dto.response.ProfesionalPresentacionDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfesionalService {
    // --- ACTUALIZADO ---
    ProfesionalResponseDTO registrarProfesional(ProfesionalRegistroDTO dto, MultipartFile foto);
    
    ProfesionalResponseDTO obtenerProfesionalPorId(Long id);

    /**
     * Búsqueda admin: {@code q}, {@code idEspecialidad}, {@code idProvincia}, {@code idLocalidad}
     * (localidad requiere provincia). Al menos un criterio obligatorio.
     */
    ProfesionalBusquedaResponseDTO buscarProfesionales(
            String texto, Long idEspecialidad, Long idProvincia, Long idLocalidad);

    /** Perfil del profesional autenticado (email del JWT). */
    ProfesionalResponseDTO obtenerProfesionalSesion(String email);

    /**
     * Pacientes, profesionales y administradores en la misma provincia y localidad que el profesional autenticado.
     * {@code texto}: apellido, nombre o DNI.
     */
    PersonaEnZonaBusquedaResponseDTO buscarPersonasEnMiUbicacion(String emailProfesional, String texto);

    /**
     * Misma búsqueda que en la ciudad del profesional, pero con provincia y localidad elegidas.
     * Requiere {@code texto}, {@code idProvincia} e {@code idLocalidad}.
     */
    PersonaEnZonaBusquedaResponseDTO buscarPersonasEnUbicacionGeneral(
            String emailProfesional, String texto, Long idProvincia, Long idLocalidad);

    /**
     * Detalle de una persona en una zona. Si {@code idProvincia} e {@code idLocalidad} son null,
     * usa la ubicación del profesional; si no, valida contra la ubicación indicada.
     */
    PersonaEnZonaDetalleDTO obtenerPersonaEnUbicacion(
            String emailProfesional, Long idUsuario, Long idProvincia, Long idLocalidad);

    PacienteCargaProfesionalResponseDTO cargarPacientePorProfesional(
            String emailProfesional, PacienteCargaProfesionalDTO dto);
    List<ProfesionalResponseDTO> obtenerTodosLosProfesionales();

    /**
     * Listado admin filtrado por nombre de membresía actual (debe existir en catálogo, p. ej. SIN_VERIFICAR, INACTIVA, ACTIVA).
     */
    List<ProfesionalResponseDTO> obtenerProfesionalesPorMembresiaNombre(String nombreMembresia);
    List<ProfesionalPresentacionDTO> listarParaPresentacion();
    /**
     * Ficha de presentación. Si {@code emailSolicitante} coincide con el profesional, se devuelve aunque no esté ACTIVO (p. ej. panel propio).
     */
    ProfesionalPresentacionDTO obtenerParaPresentacion(Long id, String emailSolicitante);

    /** True si la foto pertenece a un profesional visible en el catálogo público (estado ACTIVO, sin rol admin). */
    boolean esFotoVisibleEnCatalogoPublico(String fileName);
    List<ProfesionalResponseDTO> obtenerProfesionalesConMembresiaInactiva();
    ProfesionalResponseDTO actualizarProfesional(Long id, ProfesionalUpdateDTO dto, MultipartFile foto);
    void eliminarSoloProfesional(Long id);

    /**
     * Rechaza un profesional pendiente (SIN_VERIFICAR): valida contraseña del admin, envía correo y borra el registro.
     */
    void rechazarYBorrarProfesionalPendiente(Long idProfesional, RechazarProfesionalPendienteDTO dto, String emailAdministrador);
    
    void confirmarCuenta(String token);

    void confirmarCuentaConCodigo(String email, String codigo);

    // --- NUEVOS MÉTODOS DE MEMBRESÍA ---
    void verificarMatricula(Long idProfesional); 
    void otorgarAccesoIndefinido(Long idProfesional);
    // --- NUEVO MÉTODO ---
    void reenviarCorreoConfirmacion(String email);
}