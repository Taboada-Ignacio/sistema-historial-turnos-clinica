package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.AdministradorRegistroDTO;
import com.clinica.usuarios.dto.response.AdministradorResponseDTO;
import com.clinica.usuarios.model.Administrador;
import com.clinica.usuarios.model.Rol;
import com.clinica.usuarios.model.Sexo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AdministradorMapper {

    @Mapping(target = "idAdministrador", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "direccion", ignore = true)
    @Mapping(target = "sexo", expression = "java(mapSexo(dto.getSexo()))")
    Administrador toEntity(AdministradorRegistroDTO dto);

    @Mapping(source = "usuario.idUsuario", target = "idUsuario")
    @Mapping(source = "usuario.email", target = "email")
    @Mapping(target = "nombreLocalidad", source = "direccion.localidad.nombre")
    @Mapping(target = "idLocalidad", source = "direccion.localidad.idLocalidad")
    @Mapping(target = "nombreProvincia", source = "direccion.localidad.provincia.nombre")
    @Mapping(target = "idProvincia", source = "direccion.localidad.provincia.idProvincia")
    @Mapping(source = "usuario.estadoActual.nombre", target = "estadoActual")
    @Mapping(source = "direccion.nombre", target = "direccion")
    @Mapping(target = "roles", source = "usuario.roles", qualifiedByName = "mapRolesToStrings")
    @Mapping(target = "sexo", expression = "java(toSexoEnum(admin.getSexo()))")
    AdministradorResponseDTO toResponseDTO(Administrador admin);

    default String mapSexo(Sexo sexo) {
        return sexo != null ? sexo.name() : null;
    }

    default Sexo toSexoEnum(String sexo) {
        return sexo != null ? Sexo.valueOf(sexo) : null;
    }

    @Named("mapRolesToStrings")
    default Set<String> mapRolesToStrings(Set<Rol> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Rol::getDescripcion)
                .collect(Collectors.toSet());
    }
}
