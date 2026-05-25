package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.model.Paciente;
import com.clinica.usuarios.model.Rol;
import com.clinica.usuarios.model.Sexo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PacienteMapper {

    @Mapping(target = "idPaciente", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "obraSocial", ignore = true)
    @Mapping(target = "direccion", ignore = true)
    @Mapping(target = "sexo", expression = "java(mapSexo(dto.getSexo()))")
    Paciente toEntity(PacienteRegistroDTO dto);

    @Mapping(source = "usuario.idUsuario", target = "idUsuario")
    @Mapping(source = "usuario.email", target = "email")
    @Mapping(source = "obraSocial.descripcion", target = "nombreObraSocial")
    @Mapping(source = "obraSocial.idObraSocial", target = "idObraSocial")
    @Mapping(source = "direccion.localidad.idLocalidad", target = "idLocalidad")
    @Mapping(source = "direccion.localidad.provincia.idProvincia", target = "idProvincia")
    @Mapping(source = "direccion.localidad.nombre", target = "nombreLocalidad")
    @Mapping(source = "direccion.localidad.provincia.nombre", target = "nombreProvincia")
    @Mapping(source = "usuario.estadoActual.nombre", target = "estadoActual")
    @Mapping(source = "direccion.nombre", target = "direccion")
    @Mapping(source = "usuario.roles", target = "roles")
    @Mapping(target = "sexo", expression = "java(toSexoEnum(entity.getSexo()))")
    PacienteResponseDTO toResponseDTO(Paciente entity);

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
