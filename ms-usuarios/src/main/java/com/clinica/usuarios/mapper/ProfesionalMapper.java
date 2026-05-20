package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.model.Profesional;
import com.clinica.usuarios.model.Rol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfesionalMapper {

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "especialidad", ignore = true)
    @Mapping(target = "estadoActual", ignore = true)
    @Mapping(target = "historialEstados", ignore = true)
    // --- NUEVOS CAMPOS IGNORADOS (Se manejan en el Service) ---
    @Mapping(target = "membresiaActual", ignore = true)
    @Mapping(target = "historialMembresias", ignore = true)
    @Mapping(target = "fotoPerfil", ignore = true)
    @Mapping(target = "direccion", ignore = true)
    Profesional toEntity(ProfesionalRegistroDTO dto);

    @Mapping(source = "especialidad.descripcion", target = "especialidad")
    @Mapping(source = "direccion.localidad.nombre", target = "nombreLocalidad")
    @Mapping(source = "direccion.localidad.provincia.nombre", target = "nombreProvincia")
    @Mapping(source = "estadoActual.nombre", target = "estadoActual")
    // --- NUEVO CAMPO MAPEADO ---
    @Mapping(source = "membresiaActual.nombre", target = "membresiaActual")
    @Mapping(source = "direccion.nombre", target = "direccion")
    // Nota: fotoPerfil se mapea solo (Entity.fotoPerfil -> DTO.fotoPerfil)
    ProfesionalResponseDTO toResponseDTO(Profesional entity);

    default String mapRolToString(Rol rol) {
        if (rol == null || rol.getDescripcion() == null) return null;
        return rol.getDescripcion();
    }
}