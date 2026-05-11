package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.LocalidadRegistroDTO;
import com.clinica.usuarios.dto.request.LocalidadUpdateDTO;
import com.clinica.usuarios.dto.response.LocalidadResponseDTO;
import com.clinica.usuarios.constants.CatalogoSentinelConstants;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.LocalidadMapper;
import com.clinica.usuarios.model.Direccion;
import com.clinica.usuarios.model.Localidad;
import com.clinica.usuarios.model.Provincia;
import com.clinica.usuarios.repository.DireccionRepository;
import com.clinica.usuarios.repository.LocalidadRepository;
import com.clinica.usuarios.repository.ProvinciaRepository;
import com.clinica.usuarios.repository.UsuarioRepository;
import com.clinica.usuarios.service.LocalidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocalidadServiceImpl implements LocalidadService {

    private final LocalidadRepository localidadRepository;
    private final ProvinciaRepository provinciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DireccionRepository direccionRepository;
    private final LocalidadMapper localidadMapper;

    @Override
    @Transactional
    public LocalidadResponseDTO registrarLocalidad(LocalidadRegistroDTO dto) {
        // Normalizamos a mayúsculas para la comparación
        String nombreMayuscula = dto.getNombre().toUpperCase();

        // 1. Validar unicidad del nombre dentro de la misma provincia
        if (localidadRepository.existsByNombreAndProvinciaIdProvincia(nombreMayuscula, dto.getIdProvincia())) {
            throw new ReglaDeNegocioException("Ya existe una localidad con el nombre '" + nombreMayuscula + "' en esta provincia.");
        }

        // 2. Validar que la provincia exista
        Provincia provincia = provinciaRepository.findById(dto.getIdProvincia())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se puede crear la localidad: La provincia con ID " + dto.getIdProvincia() + " no existe."));

        Localidad localidad = localidadMapper.toEntity(dto);
        localidad.setProvincia(provincia);
        localidad.setNombre(nombreMayuscula);

        return localidadMapper.toResponseDTO(localidadRepository.save(localidad));
    }

    @Override
    @Transactional(readOnly = true)
    public LocalidadResponseDTO obtenerLocalidadPorId(Long id) {
        Localidad localidad = localidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada con ID: " + id));
        return localidadMapper.toResponseDTO(localidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalidadResponseDTO> obtenerTodasLasLocalidades() {
        return localidadRepository.findAll().stream()
                .map(localidadMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LocalidadResponseDTO actualizarLocalidad(Long id, LocalidadUpdateDTO dto) {
        Localidad localidad = localidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la localidad a actualizar con ID: " + id));

        if (esLocalidadSentinel(localidad)) {
            throw new ReglaDeNegocioException("No se puede modificar el registro reservado del sistema.");
        }

        String nuevoNombre = dto.getNombre().toUpperCase();

        // Validar si el nombre ya existe en la provincia, excluyendo la localidad actual
        // (Nota: Si el repositorio no tiene este método específico, podés usar una búsqueda y comparar IDs)
        if (localidadRepository.existsByNombreAndProvinciaIdProvincia(nuevoNombre, dto.getIdProvincia())) {
            // Buscamos si la que existe es distinta a la que estamos editando
            localidadRepository.findAll().stream()
                .filter(l -> l.getNombre().equals(nuevoNombre) 
                        && l.getProvincia().getIdProvincia().equals(dto.getIdProvincia())
                        && !l.getIdLocalidad().equals(id))
                .findFirst()
                .ifPresent(l -> {
                    throw new ReglaDeNegocioException("La localidad '" + nuevoNombre + "' ya existe en esta provincia.");
                });
        }

        Provincia provincia = provinciaRepository.findById(dto.getIdProvincia())
                .orElseThrow(() -> new RecursoNoEncontradoException("Error al actualizar: La provincia con ID " + dto.getIdProvincia() + " no existe."));

        localidad.setNombre(nuevoNombre);
        localidad.setProvincia(provincia);

        return localidadMapper.toResponseDTO(localidadRepository.save(localidad));
    }

    @Override
    @Transactional
    public void eliminarLocalidad(Long id) {
        Localidad localidad = localidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La localidad con ID " + id + " no existe."));
        if (esLocalidadSentinel(localidad)) {
            throw new ReglaDeNegocioException("No se puede eliminar el registro reservado del sistema.");
        }
        Localidad sentinel = obtenerLocalidadSentinel();
        if (sentinel.getIdLocalidad().equals(id)) {
            throw new ReglaDeNegocioException("No se puede eliminar el registro reservado del sistema.");
        }
        Long sentinelId = sentinel.getIdLocalidad();
        for (Direccion d : direccionRepository.findByLocalidad_IdLocalidadOrderByNombreAsc(id)) {
            String base = d.getNombre() == null ? "" : d.getNombre().trim();
            String candidato = base.isEmpty() ? "SIN NOMBRE" : base;
            int suf = 0;
            while (direccionRepository.existsByNombreAndLocalidad_IdLocalidad(candidato, sentinelId)) {
                suf++;
                candidato = base + " (" + suf + ")";
            }
            d.setNombre(candidato);
            d.setLocalidad(sentinel);
            direccionRepository.save(d);
        }
        usuarioRepository.reasignarLocalidad(id, sentinelId);
        localidadRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalidadResponseDTO> obtenerLocalidadesPorProvincia(Long provinciaId) {
        List<Localidad> localidades = localidadRepository.findByProvinciaId(provinciaId);
        
        // Si no hay localidades, podrías devolver una lista vacía o lanzar una excepción
        return localidades.stream()
                .map(localidadMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private boolean esLocalidadSentinel(Localidad l) {
        if (l.getProvincia() == null || l.getNombre() == null) {
            return false;
        }
        return CatalogoSentinelConstants.esSentinelNombreODescripcion(l.getNombre())
                && CatalogoSentinelConstants.esSentinelNombreODescripcion(l.getProvincia().getNombre());
    }

    private Localidad obtenerLocalidadSentinel() {
        Provincia pSent = provinciaRepository.findByNombre(CatalogoSentinelConstants.SIN_ESPECIFICAR)
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "Falta la provincia reservada '" + CatalogoSentinelConstants.SIN_ESPECIFICAR + "'."));
        return localidadRepository
                .findByNombreAndProvincia_IdProvincia(CatalogoSentinelConstants.SIN_ESPECIFICAR, pSent.getIdProvincia())
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "Falta la localidad reservada '" + CatalogoSentinelConstants.SIN_ESPECIFICAR + "'."));
    }
}