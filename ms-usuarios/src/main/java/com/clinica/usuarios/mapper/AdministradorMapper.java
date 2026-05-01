package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;
import com.clinica.usuarios.model.Administrador;
import com.clinica.usuarios.model.Rol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AdministradorMapper {

    // Ignoramos campos que setearemos manualmente en el Service
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "localidad", ignore = true)
    @Mapping(target = "estado", constant = "true") 
    Administrador toEntity(AdministradorRegistroDTO dto);

    @Mapping(target = "nombreLocalidad", source = "localidad.nombre")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesToStrings")
    AdministradorResponseDTO toResponseDTO(Administrador admin);

    @Named("mapRolesToStrings")
    default Set<String> mapRolesToStrings(Set<Rol> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(Rol::getDescripcion)
                .collect(Collectors.toSet());
    }
}