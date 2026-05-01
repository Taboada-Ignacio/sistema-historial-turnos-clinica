package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.ProvinciaRegistroDTO;
import com.clinica.usuarios.dto.response.ProvinciaResponseDTO;
import com.clinica.usuarios.model.Provincia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProvinciaMapper {

    @Mapping(target = "idProvincia", ignore = true)
    Provincia toEntity(ProvinciaRegistroDTO dto);

    ProvinciaResponseDTO toResponseDTO(Provincia entity);
}