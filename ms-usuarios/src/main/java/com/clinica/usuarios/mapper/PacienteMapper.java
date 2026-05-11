package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.model.Paciente;
import com.clinica.usuarios.model.Rol; // Importante importar Rol
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;



@Mapper(componentModel = "spring")
public interface PacienteMapper {

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "localidad", ignore = true)
    @Mapping(target = "obraSocial", ignore = true)
    @Mapping(target = "estadoActual", ignore = true) // El Service asignará "PENDIENTE"
    @Mapping(target = "direccion", ignore = true)
    @Mapping(target = "historialEstados", ignore = true)
    Paciente toEntity(PacienteRegistroDTO dto);

    @Mapping(source = "obraSocial.descripcion", target = "nombreObraSocial")
    @Mapping(source = "localidad.nombre", target = "nombreLocalidad")
    @Mapping(source = "localidad.provincia.nombre", target = "nombreProvincia")
    @Mapping(source = "estadoActual.nombre", target = "estadoActual") // Aplanamos el estado
    @Mapping(source = "direccion.nombre", target = "direccion")
    PacienteResponseDTO toResponseDTO(Paciente entity);

    default String mapRolToString(Rol rol) {
        if (rol == null || rol.getDescripcion() == null) return null;
        return rol.getDescripcion(); 
    }
}