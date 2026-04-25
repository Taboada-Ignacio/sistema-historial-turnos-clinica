package com.clinica.usuarios.mapper;

import com.clinica.usuarios.dto.request.PacienteRegistroDTO;
import com.clinica.usuarios.dto.response.PacienteResponseDTO;
import com.clinica.usuarios.model.Paciente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// componentModel = "spring" permite inyectar este mapper con @Autowired en nuestro Service
@Mapper(componentModel = "spring")
public interface PacienteMapper {

    /**
     * Convierte los datos que entran (DTO) a una Entidad para guardar en BD.
     * Ignoramos los objetos complejos (Rol, Localidad, ObraSocial) porque el
     * Service se encargará de buscarlos en la base de datos por su ID de forma segura.
     */
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "rol", ignore = true)
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
    PacienteResponseDTO toResponseDTO(Paciente entity);
}