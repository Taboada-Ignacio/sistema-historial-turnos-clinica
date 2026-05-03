package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.model.Profesional;
import com.clinica.usuarios.model.Rol; // Importante para el método default
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set; // Importante para la colección

@Mapper(componentModel = "spring")
public interface ProfesionalMapper {

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "localidad", ignore = true)
    @Mapping(target = "especialidad", ignore = true)
    @Mapping(target = "estadoActual", ignore = true)
    @Mapping(target = "historialEstados", ignore = true)
    Profesional toEntity(ProfesionalRegistroDTO dto);

    @Mapping(source = "especialidad.descripcion", target = "especialidad")
    @Mapping(source = "localidad.nombre", target = "nombreLocalidad")
    @Mapping(source = "localidad.provincia.nombre", target = "nombreProvincia")
    @Mapping(source = "estadoActual.nombre", target = "estadoActual")
    ProfesionalResponseDTO toResponseDTO(Profesional entity);

    default String mapRolToString(Rol rol) {
        if (rol == null || rol.getDescripcion() == null) return null;
        return rol.getDescripcion();
    }
}