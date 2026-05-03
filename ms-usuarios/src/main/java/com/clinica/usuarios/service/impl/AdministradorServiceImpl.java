package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.request.AdministradorUpdateDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdministradorServiceImpl implements AdministradorService {

    // Inyectamos la clave desde application.yml
    @Value("${system.registration.secret}")
    private String systemSecret;

    private final AdministradorRepository administradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final LocalidadRepository localidadRepository;
    private final AdministradorMapper administradorMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AdministradorResponseDTO registrarAdministrador(AdministradorRegistroDTO dto, String providedSecret) {

        // 1. Validar la Clave del Sistema (System Key)
        if (providedSecret == null || !providedSecret.equals(systemSecret)) {
            throw new ReglaDeNegocioException("Acceso denegado: La clave del sistema es incorrecta o no fue proporcionada.");
        }

        // 2. Validar unicidad (Email y DNI en toda la tabla usuarios)
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ReglaDeNegocioException("El correo electrónico " + dto.getEmail() + " ya está en uso.");
        }
        if (usuarioRepository.findByDni(dto.getDni()).isPresent()) {
            throw new ReglaDeNegocioException("El DNI " + dto.getDni() + " ya está registrado.");
        }

        // 3. Buscar el Rol por descripción ('ADMINISTRADOR')
        Rol rolAdmin = rolRepository.findByDescripcion("ROLE_ADMINISTRADOR")
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el rol con descripción: ROLE_ADMINISTRADOR"));

        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada con ID: " + dto.getIdLocalidad()));

        // 4. Mapear y configurar
        Administrador admin = administradorMapper.toEntity(dto);
        admin.setRoles(Set.of(rolAdmin)); // Asignación automática del rol
        admin.setLocalidad(localidad);
        
        // Encriptar la contraseña antes de guardar
        admin.setPassword(passwordEncoder.encode(dto.getPassword())); 

        // 5. Guardar
        Administrador adminGuardado = administradorRepository.save(admin);

        return administradorMapper.toResponseDTO(adminGuardado);
    }
    
    // --- MÉTODOS CRUD RESTANTES ---

    @Override
    @Transactional(readOnly = true)
    public AdministradorResponseDTO obtenerAdministradorPorId(Long id) {
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el administrador con ID: " + id));
        
        return administradorMapper.toResponseDTO(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministradorResponseDTO> obtenerTodosLosAdministradores() {
        return administradorRepository.findAll().stream()
                .map(administradorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdministradorResponseDTO actualizarAdministrador(Long id, AdministradorUpdateDTO dto) {
        
        // 1. Buscamos el admin
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado con ID: " + id));

        // 2. Actualizamos datos básicos
        admin.setNombre(dto.getNombre());
        admin.setApellido(dto.getApellido());
        admin.setDni(dto.getDni());
        admin.setEmail(dto.getEmail());
        admin.setTelefono(dto.getTelefono());
        admin.setFechaNacimiento(dto.getFechaNacimiento());
        admin.setEstado(dto.getEstado());

        // 3. Actualizamos relaciones
        if (dto.getRolesIds() != null && !dto.getRolesIds().isEmpty()) {
            Set<Rol> rolesActualizados = dto.getRolesIds().stream()
                    .map(rolId -> rolRepository.findById(rolId)
                            .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el rol con ID: " + rolId)))
                    .collect(Collectors.toSet());
            admin.setRoles(rolesActualizados);
        }

        Localidad localidad = localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Localidad no encontrada con ID: " + dto.getIdLocalidad()));
        admin.setLocalidad(localidad);

        // 4. Guardamos y retornamos
        Administrador adminActualizado = administradorRepository.save(admin);
        return administradorMapper.toResponseDTO(adminActualizado);
    }

    @Override
    @Transactional
    public void eliminarAdministrador(Long id) {
        if (!administradorRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("El administrador con ID " + id + " no fue encontrado.");
        }
        
        try {
            administradorRepository.deleteById(id);
        } catch (Exception e) {
            throw new ReglaDeNegocioException(
                "No se puede eliminar el administrador con ID " + id + 
                " porque tiene registros asociados en el sistema."
            );
        }
    }
}