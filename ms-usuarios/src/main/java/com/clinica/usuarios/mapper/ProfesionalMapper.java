package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.model.Profesional;
import com.clinica.usuarios.model.Rol;
import com.clinica.usuarios.model.Sexo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfesionalMapper {

    @Mapping(target = "idProfesional", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "especialidad", ignore = true)
    @Mapping(target = "membresiaActual", ignore = true)
    @Mapping(target = "historialMembresias", ignore = true)
    @Mapping(target = "fotoPerfil", ignore = true)
    @Mapping(target = "direccion", ignore = true)
    @Mapping(target = "sexo", expression = "java(mapSexo(dto.getSexo()))")
    Profesional toEntity(ProfesionalRegistroDTO dto);

    @Mapping(source = "usuario.idUsuario", target = "idUsuario")
    @Mapping(source = "usuario.email", target = "email")
    @Mapping(source = "especialidad.descripcion", target = "especialidad")
    @Mapping(source = "especialidad.idEspecialidad", target = "idEspecialidad")
    @Mapping(source = "direccion.localidad.idLocalidad", target = "idLocalidad")
    @Mapping(source = "direccion.localidad.provincia.idProvincia", target = "idProvincia")
    @Mapping(source = "direccion.localidad.nombre", target = "nombreLocalidad")
    @Mapping(source = "direccion.localidad.provincia.nombre", target = "nombreProvincia")
    @Mapping(source = "usuario.estadoActual.nombre", target = "estadoActual")
    @Mapping(source = "membresiaActual.nombre", target = "membresiaActual")
    @Mapping(source = "direccion.nombre", target = "direccion")
    @Mapping(source = "usuario.roles", target = "roles")
    @Mapping(target = "sexo", expression = "java(toSexoEnum(entity.getSexo()))")
    ProfesionalResponseDTO toResponseDTO(Profesional entity);

    default String mapSexo(Sexo sexo) {
        return sexo != null ? sexo.name() : null;
    }

    default Sexo toSexoEnum(String sexo) {
        return sexo != null ? Sexo.valueOf(sexo) : null;
    }

    default String mapRolToString(Rol rol) {
        if (rol == null || rol.getDescripcion() == null) {
            return null;
        }
        return rol.getDescripcion();
    }
}
