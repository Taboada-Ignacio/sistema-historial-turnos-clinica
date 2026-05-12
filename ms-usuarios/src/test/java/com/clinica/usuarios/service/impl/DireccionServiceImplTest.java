package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.model.Direccion;
import com.clinica.usuarios.model.Localidad;
import com.clinica.usuarios.repository.DireccionRepository;
import com.clinica.usuarios.repository.LocalidadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DireccionServiceImplTest {

    @Mock
    DireccionRepository direccionRepository;

    @Mock
    LocalidadRepository localidadRepository;

    @Mock
    DireccionCatalogoWriter direccionCatalogoWriter;

    @InjectMocks
    DireccionServiceImpl direccionService;

    @Test
    void obtenerOCrear_reutilizaSinIntentarGuardarCuandoYaExiste() {
        Localidad loc = Localidad.builder().idLocalidad(1L).build();
        Direccion existente = Direccion.builder().idDireccion(99L).nombre("Calle 1").localidad(loc).build();
        when(direccionRepository.findByNombreAndLocalidad_IdLocalidad("Calle 1", 1L))
                .thenReturn(Optional.of(existente));

        Direccion r = direccionService.obtenerOCrearPorTextoYLocalidad("  Calle   1  ", loc);

        assertThat(r).isSameAs(existente);
        verify(direccionCatalogoWriter, never()).intentarGuardar(any());
    }

    @Test
    void obtenerOCrear_insertaEnCatalogoCuandoNoExiste() {
        Localidad loc = Localidad.builder().idLocalidad(2L).build();
        Direccion nueva = Direccion.builder().idDireccion(3L).nombre("Nueva 10").localidad(loc).build();
        when(direccionRepository.findByNombreAndLocalidad_IdLocalidad(eq("Nueva 10"), eq(2L)))
                .thenReturn(Optional.empty());
        when(direccionCatalogoWriter.intentarGuardar(any(Direccion.class))).thenReturn(Optional.of(nueva));

        Direccion r = direccionService.obtenerOCrearPorTextoYLocalidad("Nueva 10", loc);

        assertThat(r).isSameAs(nueva);
    }

    @Test
    void obtenerOCrear_trasFalloDeInsercionRecuperaFilaExistente() {
        Localidad loc = Localidad.builder().idLocalidad(5L).build();
        Direccion existente = Direccion.builder().idDireccion(7L).nombre("Dup").localidad(loc).build();
        when(direccionRepository.findByNombreAndLocalidad_IdLocalidad("Dup", 5L))
                .thenReturn(Optional.empty(), Optional.of(existente));
        when(direccionCatalogoWriter.intentarGuardar(any(Direccion.class))).thenReturn(Optional.empty());

        Direccion r = direccionService.obtenerOCrearPorTextoYLocalidad("Dup", loc);

        assertThat(r).isSameAs(existente);
    }

    @Test
    void obtenerOCrear_lanzaSiNoHayFormaDeResolver() {
        Localidad loc = Localidad.builder().idLocalidad(8L).build();
        when(direccionRepository.findByNombreAndLocalidad_IdLocalidad("X", 8L))
                .thenReturn(Optional.empty(), Optional.empty());
        when(direccionCatalogoWriter.intentarGuardar(any(Direccion.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> direccionService.obtenerOCrearPorTextoYLocalidad("X", loc))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("No se pudo registrar");
    }
}
