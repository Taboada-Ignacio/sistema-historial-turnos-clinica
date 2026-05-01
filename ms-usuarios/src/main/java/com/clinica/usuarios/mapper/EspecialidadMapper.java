package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.EspecialidadRegistroDTO;
import com.clinica.usuarios.dto.response.EspecialidadResponseDTO;
import com.clinica.usuarios.model.Especialidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EspecialidadMapper {

    // Ignoramos el ID para que Hibernate lo genere automáticamente al guardar
    @Mapping(target = "idEspecialidad", ignore = true) 
    Especialidad toEntity(EspecialidadRegistroDTO dto);

    EspecialidadResponseDTO toResponseDTO(Especialidad entity);
}