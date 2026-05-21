package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.response.EstadoDTO;
import com.clinica.usuarios.model.Estado;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EstadoMapper {

    EstadoDTO toDTO(Estado entity);
}
