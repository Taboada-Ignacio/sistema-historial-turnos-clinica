package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.dto.response.LocalidadResponseDTO;
import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.mapper.LocalidadMapper;
import com.clinica.usuarios.model.Localidad;
import com.clinica.usuarios.model.Provincia;
import com.clinica.usuarios.repository.DireccionRepository;
import com.clinica.usuarios.repository.LocalidadRepository;
import com.clinica.usuarios.repository.ProvinciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalidadServiceImplTest {

    @Mock
    LocalidadRepository localidadRepository;
    @Mock
    ProvinciaRepository provinciaRepository;
    @Mock
    DireccionRepository direccionRepository;
    @Mock
    DireccionSentinelHelper direccionSentinelHelper;
    @Mock
    LocalidadMapper localidadMapper;

    @InjectMocks
    LocalidadServiceImpl localidadService;

    @Test
    void buscar_sinFiltros_pasaNullsAlRepositorio() {
        when(localidadRepository.buscar(isNull(), isNull())).thenReturn(List.of());

        localidadService.buscarLocalidades(null, null);

        verify(localidadRepository).buscar(isNull(), isNull());
    }

    @Test
    void buscar_porProvinciaInexistente_lanza404() {
        when(provinciaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> localidadService.buscarLocalidades(99L, null))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("provincia");
    }

    @Test
    void buscar_porNombre_normalizaPatronLike() {
        Localidad loc = Localidad.builder()
                .idLocalidad(1L)
                .nombre("CORDOBA CAPITAL")
                .provincia(Provincia.builder().idProvincia(6L).nombre("CORDOBA").build())
                .build();
        when(localidadRepository.buscar(isNull(), eq("%CORD%"))).thenReturn(List.of(loc));
        when(localidadMapper.toResponseDTO(loc)).thenReturn(
                LocalidadResponseDTO.builder().idLocalidad(1L).nombre("CORDOBA CAPITAL").build());

        List<LocalidadResponseDTO> result = localidadService.buscarLocalidades(null, "  cord ");

        assertThat(result).hasSize(1);
        ArgumentCaptor<String> pattern = ArgumentCaptor.forClass(String.class);
        verify(localidadRepository).buscar(isNull(), pattern.capture());
        assertThat(pattern.getValue()).isEqualTo("%CORD%");
    }

    @Test
    void buscar_provinciaYNombre_validaProvinciaYDelega() {
        when(provinciaRepository.existsById(6L)).thenReturn(true);
        when(localidadRepository.buscar(6L, "%CAP%")).thenReturn(List.of());

        localidadService.buscarLocalidades(6L, "cap");

        verify(localidadRepository).buscar(6L, "%CAP%");
    }
}
