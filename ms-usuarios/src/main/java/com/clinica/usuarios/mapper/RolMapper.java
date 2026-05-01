package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.RolRegistroDTO;
import com.clinica.usuarios.dto.response.RolResponseDTO;
import com.clinica.usuarios.model.Rol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RolMapper {

    @Mapping(target = "idRol", ignore = true)
    Rol toEntity(RolRegistroDTO dto);

    RolResponseDTO toResponseDTO(Rol entity);
}