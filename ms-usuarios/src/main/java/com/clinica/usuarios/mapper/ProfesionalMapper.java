package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.ProfesionalRegistroDTO;
import com.clinica.usuarios.dto.response.ProfesionalResponseDTO;
import com.clinica.usuarios.model.Profesional;
import com.clinica.usuarios.model.Rol; // Importante para el método default
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set; // Importante para la colección

@Mapper(componentModel = "spring")
public interface ProfesionalMapper {

    /**
     * De DTO a Entidad. Ignoramos la Especialidad, los Roles y la Localidad 
     * para buscarlos de forma segura en el Service.
     */
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "roles", ignore = true) // CAMBIO: "rol" por "roles"
    @Mapping(target = "localidad", ignore = true)
    @Mapping(target = "especialidad", ignore = true)
    @Mapping(target = "estado", constant = "true")
    Profesional toEntity(ProfesionalRegistroDTO dto);

    /**
     * De Entidad a DTO (Respuesta para el Frontend)
     */
    @Mapping(source = "especialidad.descripcion", target = "especialidad")
    @Mapping(source = "localidad.nombre", target = "nombreLocalidad")
    @Mapping(source = "localidad.provincia.nombre", target = "nombreProvincia")
    ProfesionalResponseDTO toResponseDTO(Profesional entity);

    /**
     * Método auxiliar para que MapStruct sepa cómo convertir
     * cada objeto Rol del Set<Rol> en un simple String (su nombre)
     * para rellenar el Set<String> roles del DTO.
     */
    default String mapRolToString(Rol rol) {
        if (rol == null || rol.getDescripcion() == null) { // <-- AQUÍ
            return null;
        }
        return rol.getDescripcion(); // <-- Y AQUÍ
    }
}