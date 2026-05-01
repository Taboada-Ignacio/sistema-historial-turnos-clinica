package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.ObraSocialRegistroDTO;
import com.clinica.usuarios.dto.response.ObraSocialResponseDTO;
import com.clinica.usuarios.model.ObraSocial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ObraSocialMapper {

    // Ignoramos el ID al crear para que la base de datos lo genere solo
    @Mapping(target = "idObraSocial", ignore = true) 
    ObraSocial toEntity(ObraSocialRegistroDTO dto);

    ObraSocialResponseDTO toResponseDTO(ObraSocial entity);
}