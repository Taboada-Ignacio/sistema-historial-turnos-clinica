package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.response.CambioEstadoDTO;
import com.clinica.usuarios.model.CambioEstado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CambioEstadoMapper {

    @Mapping(target = "nombreEstado", source = "estado.nombre")
    @Mapping(target = "fecha", source = "fecha")
    CambioEstadoDTO toDTO(CambioEstado entity);
}