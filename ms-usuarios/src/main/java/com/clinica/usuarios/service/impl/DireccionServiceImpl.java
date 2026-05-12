package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.DireccionRegistroDTO;
import com.clinica.usuarios.dto.response.DireccionResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.model.Direccion;
import com.clinica.usuarios.model.Localidad;
import com.clinica.usuarios.repository.DireccionRepository;
import com.clinica.usuarios.repository.LocalidadRepository;
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
    private final DireccionCatalogoWriter direccionCatalogoWriter;

    @Override
    @Transactional(readOnly = true)
    public List<DireccionResponseDTO> listarPorLocalidad(Long idLocalidad) {
        return direccionRepository.findByLocalidad_IdLocalidadOrderByNombreAsc(idLocalidad).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public DireccionResponseDTO registrar(DireccionRegistroDTO dto) {
        String nombre = DireccionTextoNormalizer.normalizar(dto.getNombre());
        if (nombre.isEmpty()) {
            throw new ReglaDeNegocioException("El nombre de la dirección no puede estar vacío.");
        }
        if (nombre.length() > 500) {
            throw new ReglaDeNegocioException("La dirección no puede superar los 500 caracteres.");
        }
        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada con ID: " + dto.getIdLocalidad()));
        if (direccionRepository.existsByNombreAndLocalidad_IdLocalidad(nombre, dto.getIdLocalidad())) {
            throw new ReglaDeNegocioException("Ya existe una dirección con ese nombre en la localidad seleccionada.");
        }
        Direccion nueva = Direccion.builder()
                .nombre(nombre)
                .localidad(localidad)
                .build();
        return direccionCatalogoWriter.intentarGuardar(nueva)
                .map(this::toDto)
                .or(() -> direccionRepository.findByNombreAndLocalidad_IdLocalidad(nombre, dto.getIdLocalidad()).map(this::toDto))
                .orElseThrow(() -> new ReglaDeNegocioException("Ya existe una dirección con ese nombre en la localidad seleccionada."));
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
        Long idLoc = localidad.getIdLocalidad();
        return direccionRepository.findByNombreAndLocalidad_IdLocalidad(nombre, idLoc)
                .or(() -> direccionCatalogoWriter.intentarGuardar(Direccion.builder()
                        .nombre(nombre)
                        .localidad(localidad)
                        .build()))
                .or(() -> direccionRepository.findByNombreAndLocalidad_IdLocalidad(nombre, idLoc))
                .orElseThrow(() -> new ReglaDeNegocioException("No se pudo registrar la dirección. Intente nuevamente."));
    }

    private DireccionResponseDTO toDto(Direccion d) {
        return DireccionResponseDTO.builder()
                .idDireccion(d.getIdDireccion())
                .nombre(d.getNombre())
                .idLocalidad(d.getLocalidad().getIdLocalidad())
                .build();
    }
}
