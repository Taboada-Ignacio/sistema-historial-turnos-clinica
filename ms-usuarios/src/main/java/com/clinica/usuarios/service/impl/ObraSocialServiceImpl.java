package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.ObraSocialRegistroDTO;
import com.clinica.usuarios.dto.request.ObraSocialUpdateDTO;
import com.clinica.usuarios.dto.response.ObraSocialResponseDTO;
import com.clinica.usuarios.constants.CatalogoSentinelConstants;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.ObraSocialMapper;
import com.clinica.usuarios.model.ObraSocial;
import com.clinica.usuarios.repository.ObraSocialRepository;
import com.clinica.usuarios.repository.PacienteRepository;
import com.clinica.usuarios.service.ObraSocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ObraSocialServiceImpl implements ObraSocialService {

    private final ObraSocialRepository obraSocialRepository;
    private final PacienteRepository pacienteRepository;
    private final ObraSocialMapper obraSocialMapper;

    @Override
    @Transactional
    public ObraSocialResponseDTO registrarObraSocial(ObraSocialRegistroDTO dto) {
        
        String descripcionMayuscula = dto.getDescripcion().toUpperCase();

        if (obraSocialRepository.findByDescripcion(descripcionMayuscula).isPresent()) {
            throw new ReglaDeNegocioException("La Obra Social '" + descripcionMayuscula + "' ya existe en el sistema.");
        }

        ObraSocial obraSocial = obraSocialMapper.toEntity(dto);
        obraSocial.setDescripcion(descripcionMayuscula);
        
        ObraSocial obraSocialGuardada = obraSocialRepository.save(obraSocial);
        return obraSocialMapper.toResponseDTO(obraSocialGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ObraSocialResponseDTO obtenerObraSocialPorId(Long id) {
        ObraSocial obraSocial = obraSocialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la Obra Social con ID: " + id));
        
        return obraSocialMapper.toResponseDTO(obraSocial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObraSocialResponseDTO> obtenerTodasLasObrasSociales() {
        return obraSocialRepository.findAll().stream()
                .map(obraSocialMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ObraSocialResponseDTO actualizarObraSocial(Long id, ObraSocialUpdateDTO dto) {
        
        ObraSocial obraSocial = obraSocialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Obra Social no encontrada con ID: " + id));

        if (CatalogoSentinelConstants.esSentinelNombreODescripcion(obraSocial.getDescripcion())) {
            throw new ReglaDeNegocioException("No se puede modificar el registro reservado del sistema.");
        }

        String nuevaDescripcion = dto.getDescripcion().toUpperCase();

        obraSocialRepository.findByDescripcion(nuevaDescripcion).ifPresent(existente -> {
            if (!existente.getIdObraSocial().equals(id)) {
                throw new ReglaDeNegocioException("La descripción '" + nuevaDescripcion + "' ya está registrada en otra Obra Social.");
            }
        });

        obraSocial.setDescripcion(nuevaDescripcion);
        obraSocial = obraSocialRepository.save(obraSocial);

        return obraSocialMapper.toResponseDTO(obraSocial);
    }

    @Override
    @Transactional
    public void eliminarObraSocial(Long id) {
        ObraSocial obra = obraSocialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La Obra Social con ID " + id + " no fue encontrada."));
        if (CatalogoSentinelConstants.esSentinelNombreODescripcion(obra.getDescripcion())) {
            throw new ReglaDeNegocioException("No se puede eliminar el registro reservado del sistema.");
        }
        ObraSocial sentinel = obraSocialRepository.findByDescripcion(CatalogoSentinelConstants.SIN_ESPECIFICAR)
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "Falta la obra social reservada '" + CatalogoSentinelConstants.SIN_ESPECIFICAR + "' en la base de datos."));
        if (sentinel.getIdObraSocial().equals(id)) {
            throw new ReglaDeNegocioException("No se puede eliminar el registro reservado del sistema.");
        }
        pacienteRepository.reasignarObraSocial(id, sentinel.getIdObraSocial());
        obraSocialRepository.deleteById(id);
    }
}