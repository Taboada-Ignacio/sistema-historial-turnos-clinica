package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.ProvinciaRegistroDTO;
import com.clinica.usuarios.dto.request.ProvinciaUpdateDTO;
import com.clinica.usuarios.dto.response.ProvinciaResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.ProvinciaMapper;
import com.clinica.usuarios.model.Provincia;
import com.clinica.usuarios.repository.ProvinciaRepository;
import com.clinica.usuarios.service.ProvinciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvinciaServiceImpl implements ProvinciaService {

    private final ProvinciaRepository provinciaRepository;
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
        if (!provinciaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("La provincia con ID " + id + " no fue encontrada.");
        }

        try {
            provinciaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaDeNegocioException(
                "No se puede eliminar la provincia porque tiene localidades asociadas a ella."
            );
        }
    }
}