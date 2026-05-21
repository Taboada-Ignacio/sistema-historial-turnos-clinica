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

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "estadoActual", ignore = true) // El Service asignará el objeto Estado "ACTIVO"
    @Mapping(target = "direccion", ignore = true)
    @Mapping(target = "historialEstados", ignore = true)
    Administrador toEntity(AdministradorRegistroDTO dto);

    @Mapping(target = "nombreLocalidad", source = "direccion.localidad.nombre")
    @Mapping(target = "idLocalidad", source = "direccion.localidad.idLocalidad")
    @Mapping(target = "nombreProvincia", source = "direccion.localidad.provincia.nombre")
    @Mapping(target = "idProvincia", source = "direccion.localidad.provincia.idProvincia")
    @Mapping(target = "estadoActual", source = "estadoActual.nombre")
    @Mapping(source = "direccion.nombre", target = "direccion")
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