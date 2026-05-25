package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.DireccionRegistroDTO;
import com.clinica.usuarios.dto.request.DireccionUpdateDTO;
import com.clinica.usuarios.dto.response.DireccionResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.model.Direccion;
import com.clinica.usuarios.model.Localidad;
import com.clinica.usuarios.repository.DireccionRepository;
import com.clinica.usuarios.repository.LocalidadRepository;
import com.clinica.usuarios.repository.AdministradorRepository;
import com.clinica.usuarios.repository.PacienteRepository;
import com.clinica.usuarios.repository.ProfesionalRepository;
import com.clinica.usuarios.service.DireccionService;
import com.clinica.usuarios.util.DireccionTextoNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DireccionServiceImpl implements DireccionService {

    private final DireccionRepository direccionRepository;
    private final LocalidadRepository localidadRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final AdministradorRepository administradorRepository;
    private final DireccionCatalogoWriter direccionCatalogoWriter;
    private final DireccionSentinelHelper direccionSentinelHelper;

    @Override
    @Transactional(readOnly = true)
    public List<DireccionResponseDTO> listarPorLocalidad(Long idLocalidad) {
        return direccionRepository.findByLocalidad_IdLocalidadOrderByNombreAsc(idLocalidad).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DireccionResponseDTO obtenerPorId(Long id) {
        return toDto(buscarPorId(id));
    }

    @Override
    @Transactional
    public DireccionResponseDTO registrar(DireccionRegistroDTO dto) {
        String nombre = normalizarYNormalizarNombre(dto.getNombre());
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada con ID: " + dto.getIdLocalidad()));
        return toDto(guardarNuevaDireccion(nombre, localidad));
    }

    @Override
    @Transactional
    public DireccionResponseDTO actualizar(Long id, DireccionUpdateDTO dto) {
        Direccion direccion = buscarPorId(id);
        if (direccionSentinelHelper.esDireccionSentinel(direccion)) {
            throw new ReglaDeNegocioException("No se puede modificar el registro reservado del sistema.");
        }
        String nombre = normalizarYNormalizarNombre(dto.getNombre());
        Long idLocalidad = direccion.getLocalidad().getIdLocalidad();
        if (direccionRepository.existsByNombreAndLocalidad_IdLocalidad(nombre, idLocalidad)
                && !nombre.equalsIgnoreCase(direccion.getNombre())) {
            throw new ReglaDeNegocioException("Ya existe una dirección con ese nombre en la localidad seleccionada.");
        }
        direccion.setNombre(nombre);
        return toDto(direccionRepository.save(direccion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Direccion direccion = buscarPorId(id);
        if (direccionSentinelHelper.esDireccionSentinel(direccion)) {
            throw new ReglaDeNegocioException("No se puede eliminar el registro reservado del sistema.");
        }
        Long idLocalidad = direccion.getLocalidad().getIdLocalidad();
        Direccion sentinel = direccionSentinelHelper.obtenerSentinelPorIdLocalidad(idLocalidad);
        pacienteRepository.reasignarDireccion(id, sentinel.getIdDireccion());
        profesionalRepository.reasignarDireccion(id, sentinel.getIdDireccion());
        administradorRepository.reasignarDireccion(id, sentinel.getIdDireccion());
        direccionRepository.delete(direccion);
    }

    @Override
    @Transactional
    public Direccion obtenerOCrearPorTextoYLocalidad(String texto, Localidad localidad) {
        if (localidad == null || localidad.getIdLocalidad() == null) {
            throw new ReglaDeNegocioException("La localidad es obligatoria para registrar la dirección.");
        }
        String nombre = DireccionTextoNormalizer.normalizar(texto);
        if (nombre.isEmpty()) {
            throw new ReglaDeNegocioException("La dirección es obligatoria.");
        }
        if (nombre.length() > 500) {
            throw new ReglaDeNegocioException("La dirección no puede superar los 500 caracteres.");
        }
        if (com.clinica.usuarios.constants.CatalogoSentinelConstants.esSentinelNombreODescripcion(nombre)) {
            return direccionSentinelHelper.obtenerOCrearSentinel(localidad);
        }
        Long idLoc = localidad.getIdLocalidad();
        return direccionRepository.findByNombreAndLocalidad_IdLocalidad(nombre, idLoc)
                .or(() -> direccionCatalogoWriter.intentarGuardar(Direccion.builder()
                        .nombre(nombre)
                        .localidad(localidad)
                        .build()))
                .or(() -> direccionRepository.findByNombreAndLocalidad_IdLocalidad(nombre, idLoc))
                .orElseThrow(() -> new ReglaDeNegocioException("No se pudo registrar la dirección. Intente nuevamente."));
    }

    private Direccion buscarPorId(Long id) {
        return direccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección no encontrada con ID: " + id));
    }

    private String normalizarYNormalizarNombre(String texto) {
        String nombre = DireccionTextoNormalizer.normalizar(texto);
        if (nombre.isEmpty()) {
            throw new ReglaDeNegocioException("El nombre de la dirección no puede estar vacío.");
        }
        if (nombre.length() > 500) {
            throw new ReglaDeNegocioException("La dirección no puede superar los 500 caracteres.");
        }
        direccionSentinelHelper.validarNombreNoEsSentinel(nombre);
        return nombre;
    }

    private Direccion guardarNuevaDireccion(String nombre, Localidad localidad) {
        Long idLocalidad = localidad.getIdLocalidad();
        if (direccionRepository.existsByNombreAndLocalidad_IdLocalidad(nombre, idLocalidad)) {
            throw new ReglaDeNegocioException("Ya existe una dirección con ese nombre en la localidad seleccionada.");
        }
        Direccion nueva = Direccion.builder()
                .nombre(nombre)
                .localidad(localidad)
                .build();
        return direccionCatalogoWriter.intentarGuardar(nueva)
                .or(() -> direccionRepository.findByNombreAndLocalidad_IdLocalidad(nombre, idLocalidad))
                .orElseThrow(() -> new ReglaDeNegocioException("Ya existe una dirección con ese nombre en la localidad seleccionada."));
    }

    private DireccionResponseDTO toDto(Direccion d) {
        return DireccionResponseDTO.builder()
                .idDireccion(d.getIdDireccion())
                .nombre(d.getNombre())
                .idLocalidad(d.getLocalidad().getIdLocalidad())
                .build();
    }
}
