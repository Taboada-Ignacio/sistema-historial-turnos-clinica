package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.mapper.AdministradorMapper;
import com.clinica.usuarios.model.Administrador;
import com.clinica.usuarios.model.Localidad;
import com.clinica.usuarios.model.Rol;
import com.clinica.usuarios.repository.AdministradorRepository;
import com.clinica.usuarios.repository.LocalidadRepository;
import com.clinica.usuarios.repository.RolRepository;
import com.clinica.usuarios.repository.UsuarioRepository;
import com.clinica.usuarios.service.AdministradorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdministradorServiceImpl implements AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final LocalidadRepository localidadRepository;
    private final AdministradorMapper administradorMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AdministradorResponseDTO registrarAdministrador(AdministradorRegistroDTO dto) {

        // 1. Validar unicidad (Email y DNI en toda la tabla usuarios)
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ReglaDeNegocioException("El correo electrónico " + dto.getEmail() + " ya está en uso.");
        }
        if (usuarioRepository.findByDni(dto.getDni()).isPresent()) {
            throw new ReglaDeNegocioException("El DNI " + dto.getDni() + " ya está registrado.");
        }

        // 2. Buscar dependencias
        Set<Rol> rolesAsignados = dto.getRolesIds().stream()
                .map(rolId -> rolRepository.findById(rolId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + rolId)))
                .collect(Collectors.toSet());

        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada con ID: " + dto.getIdLocalidad()));

        // 3. Mapear y configurar
        Administrador admin = administradorMapper.toEntity(dto);
        admin.setRoles(rolesAsignados);
        admin.setLocalidad(localidad);
        // ¡ENCRIPTAMOS LA CONTRASEÑA!
        admin.setPassword(passwordEncoder.encode(dto.getPassword())); 

        // 4. Guardar
        Administrador adminGuardado = administradorRepository.save(admin);

        return administradorMapper.toResponseDTO(adminGuardado);
    }
}