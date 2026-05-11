package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.LocalidadRegistroDTO;
import com.clinica.usuarios.dto.response.LocalidadResponseDTO;
import com.clinica.usuarios.model.Localidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocalidadMapper {

    @Mapping(target = "idLocalidad", ignore = true)
    @Mapping(target = "provincia", ignore = true) // Se asigna manualmente en el Service
    Localidad toEntity(LocalidadRegistroDTO dto);

    @Mapping(source = "provincia.idProvincia", target = "idProvincia")
    @Mapping(source = "provincia.nombre", target = "nombreProvincia")
    LocalidadResponseDTO toResponseDTO(Localidad entity);
}