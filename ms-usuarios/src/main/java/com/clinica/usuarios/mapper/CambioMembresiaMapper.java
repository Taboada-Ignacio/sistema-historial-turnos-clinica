package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.response.CambioMembresiaResponseDTO;
import com.clinica.usuarios.model.CambioMembresia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CambioMembresiaMapper {

    // Extraemos el string del objeto Membresia anidado
    @Mapping(source = "membresia.nombre", target = "nombreMembresia")
    CambioMembresiaResponseDTO toResponseDTO(CambioMembresia entity);
}