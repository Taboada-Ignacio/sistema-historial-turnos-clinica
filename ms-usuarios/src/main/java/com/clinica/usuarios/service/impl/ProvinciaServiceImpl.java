package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.ProvinciaRegistroDTO;
import com.clinica.usuarios.dto.request.ProvinciaUpdateDTO;
import com.clinica.usuarios.dto.response.ProvinciaResponseDTO;
import com.clinica.usuarios.constants.CatalogoSentinelConstants;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.ProvinciaMapper;
import com.clinica.usuarios.model.Localidad;
import com.clinica.usuarios.model.Provincia;
import com.clinica.usuarios.repository.LocalidadRepository;
import com.clinica.usuarios.repository.ProvinciaRepository;
import com.clinica.usuarios.service.ProvinciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvinciaServiceImpl implements ProvinciaService {

    private final ProvinciaRepository provinciaRepository;
    private final LocalidadRepository localidadRepository;
    private final ProvinciaMapper provinciaMapper;

    @Override
    @Transactional
    public ProvinciaResponseDTO registrarProvincia(ProvinciaRegistroDTO dto) {
        
        String nombreMayuscula = dto.getNombre().toUpperCase();

        if (provinciaRepository.findByNombre(nombreMayuscula).isPresent()) {
            throw new ReglaDeNegocioException("La provincia '" + nombreMayuscula + "' ya está registrada.");
        }

        Provincia provincia = provinciaMapper.toEntity(dto);
        provincia.setNombre(nombreMayuscula);
        
        Provincia provinciaGuardada = provinciaRepository.save(provincia);
        return provinciaMapper.toResponseDTO(provinciaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ProvinciaResponseDTO obtenerProvinciaPorId(Long id) {
        Provincia provincia = provinciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la provincia con ID: " + id));
        
        return provinciaMapper.toResponseDTO(provincia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinciaResponseDTO> obtenerTodasLasProvincias() {
        return provinciaRepository.findAll().stream()
                .map(provinciaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProvinciaResponseDTO actualizarProvincia(Long id, ProvinciaUpdateDTO dto) {
        
        Provincia provincia = provinciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Provincia no encontrada con ID: " + id));

        if (CatalogoSentinelConstants.esSentinelNombreODescripcion(provincia.getNombre())) {
            throw new ReglaDeNegocioException("No se puede modificar el registro reservado del sistema.");
        }

        String nuevoNombre = dto.getNombre().toUpperCase();

        provinciaRepository.findByNombre(nuevoNombre).ifPresent(existente -> {
            if (!existente.getIdProvincia().equals(id)) {
                throw new ReglaDeNegocioException("El nombre '" + nuevoNombre + "' ya pertenece a otra provincia.");
            }
        });

        provincia.setNombre(nuevoNombre);
        provincia = provinciaRepository.save(provincia);

        return provinciaMapper.toResponseDTO(provincia);
    }

    @Override
    @Transactional
    public void eliminarProvincia(Long id) {
        Provincia provincia = provinciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La provincia con ID " + id + " no fue encontrada."));
        if (CatalogoSentinelConstants.esSentinelNombreODescripcion(provincia.getNombre())) {
            throw new ReglaDeNegocioException("No se puede eliminar el registro reservado del sistema.");
        }
        Provincia sentinel = provinciaRepository.findByNombre(CatalogoSentinelConstants.SIN_ESPECIFICAR)
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "Falta la provincia reservada '" + CatalogoSentinelConstants.SIN_ESPECIFICAR + "' en la base de datos."));
        if (sentinel.getIdProvincia().equals(id)) {
            throw new ReglaDeNegocioException("No se puede eliminar el registro reservado del sistema.");
        }
        Long sentinelId = sentinel.getIdProvincia();
        List<Localidad> mover = new ArrayList<>(localidadRepository.findByProvinciaId(id));
        for (Localidad loc : mover) {
            String base = loc.getNombre();
            String candidato = base;
            int suf = 0;
            while (localidadRepository.existsByNombreAndProvinciaIdProvincia(candidato, sentinelId)) {
                suf++;
                candidato = base + " (" + suf + ")";
            }
            loc.setNombre(candidato);
            loc.setProvincia(sentinel);
            localidadRepository.save(loc);
        }
        provinciaRepository.deleteById(id);
    }
}