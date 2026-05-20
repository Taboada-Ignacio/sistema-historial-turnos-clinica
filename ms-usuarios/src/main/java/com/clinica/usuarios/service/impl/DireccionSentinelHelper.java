package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.constants.CatalogoSentinelConstants;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.model.Direccion;
import com.clinica.usuarios.model.Localidad;
import com.clinica.usuarios.repository.DireccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DireccionSentinelHelper {

    private final DireccionRepository direccionRepository;

    public void validarNombreNoEsSentinel(String nombreNormalizado) {
        if (CatalogoSentinelConstants.esSentinelNombreODescripcion(nombreNormalizado)) {
            throw new ReglaDeNegocioException(
                    "No se puede crear ni modificar manualmente el registro reservado del sistema.");
        }
    }

    public boolean esDireccionSentinel(Direccion direccion) {
        return direccion != null
                && CatalogoSentinelConstants.esSentinelNombreODescripcion(direccion.getNombre());
    }

    public Direccion obtenerOCrearSentinel(Localidad localidad) {
        Long idLocalidad = localidad.getIdLocalidad();
        return direccionRepository
                .findByNombreAndLocalidad_IdLocalidad(CatalogoSentinelConstants.SIN_ESPECIFICAR, idLocalidad)
                .orElseGet(() -> direccionRepository.save(Direccion.builder()
                        .nombre(CatalogoSentinelConstants.SIN_ESPECIFICAR)
                        .localidad(localidad)
                        .build()));
    }

    public Direccion obtenerSentinelPorIdLocalidad(Long idLocalidad) {
        return direccionRepository
                .findByNombreAndLocalidad_IdLocalidad(CatalogoSentinelConstants.SIN_ESPECIFICAR, idLocalidad)
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "Falta la dirección reservada 'SIN ESPECIFICAR' para la localidad ID: " + idLocalidad));
    }
}
