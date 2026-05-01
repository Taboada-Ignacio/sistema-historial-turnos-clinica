package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.model.Paciente;
import com.clinica.usuarios.model.Rol; // Importante importar Rol
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

// componentModel = "spring" permite inyectar este mapper con @Autowired en nuestro Service
@Mapper(componentModel = "spring")
public interface PacienteMapper {

    /**
     * Convierte los datos que entran (DTO) a una Entidad para guardar en BD.
     * Ignoramos los objetos complejos (Roles, Localidad, ObraSocial) porque el
     * Service se encargará de buscarlos en la base de datos por su ID de forma segura.
     */
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "roles", ignore = true) // CAMBIO 1: Ahora se llama 'roles' (en plural)
    @Mapping(target = "localidad", ignore = true)
    @Mapping(target = "obraSocial", ignore = true)
    @Mapping(target = "estado", constant = "true") // Por defecto, el paciente nace activo
    Paciente toEntity(PacienteRegistroDTO dto);

    /**
     * Convierte la Entidad de la BD a un "sobre" seguro (DTO) para el Frontend.
     * Aquí hacemos la magia de "aplanar" las relaciones para sacar solo los nombres.
     */
    @Mapping(source = "obraSocial.descripcion", target = "nombreObraSocial")
    @Mapping(source = "localidad.nombre", target = "nombreLocalidad")
    @Mapping(source = "localidad.provincia.nombre", target = "nombreProvincia")
    // CAMBIO 2: MapStruct necesita saber cómo pasar de un Set<Rol> a un Set<String>.
    // Como los dos campos se llaman "roles" (en Entity y DTO), MapStruct intentará mapearlos,
    // pero usará el método de abajo (mapRolToString) para transformar cada elemento de la lista.
    PacienteResponseDTO toResponseDTO(Paciente entity);

    /**
     * CAMBIO 3: Método auxiliar (default) para enseñarle a MapStruct
     * cómo convertir un objeto Rol en un simple String (su nombre).
     * MapStruct usará esto automáticamente cuando mapee colecciones.
     */
    default String mapRolToString(Rol rol) {
        if (rol == null || rol.getDescripcion() == null) {
            return null;
        }
        return rol.getDescripcion(); 
    }
}