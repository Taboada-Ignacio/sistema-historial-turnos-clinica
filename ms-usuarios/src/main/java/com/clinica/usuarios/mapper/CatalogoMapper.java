package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.response.CatalogoResponseDTO;
import com.clinica.usuarios.dto.response.LocalidadResponseDTO;
import com.clinica.usuarios.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CatalogoMapper {

    @Mapping(source = "idRol", target = "id")
    CatalogoResponseDTO toDTO(Rol entity);

    @Mapping(source = "idEspecialidad", target = "id")
    CatalogoResponseDTO toDTO(Especialidad entity);

    @Mapping(source = "idObraSocial", target = "id")
    CatalogoResponseDTO toDTO(ObraSocial entity);

    // Para Provincia, mapeamos 'nombre' a 'descripcion' para que sea compatible con el DTO genérico
    @Mapping(source = "idProvincia", target = "id")
    @Mapping(source = "nombre", target = "descripcion")
    CatalogoResponseDTO toDTO(Provincia entity);

    // Para Localidad, usamos su propio DTO para no perder la relación con la Provincia
    @Mapping(source = "idLocalidad", target = "id")
    @Mapping(source = "provincia.idProvincia", target = "idProvincia")
    LocalidadResponseDTO toLocalidadDTO(Localidad entity);
}