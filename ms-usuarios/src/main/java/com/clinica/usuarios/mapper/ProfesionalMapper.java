package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.model.Profesional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfesionalMapper {

    /**
     * De DTO a Entidad. Ignoramos la Especialidad, el Rol y la Localidad 
     * para buscarlos de forma segura en el Service.
     */
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "localidad", ignore = true)
    @Mapping(target = "especialidad", ignore = true)
    @Mapping(target = "estado", constant = "true")
    Profesional toEntity(ProfesionalRegistroDTO dto);

    /**
     * De Entidad a DTO (Respuesta para el Frontend)
     */
    @Mapping(source = "especialidad.descripcion", target = "especialidad")
    @Mapping(source = "localidad.nombre", target = "nombreLocalidad")
    @Mapping(source = "localidad.provincia.nombre", target = "nombreProvincia")
    ProfesionalResponseDTO toResponseDTO(Profesional entity);
}