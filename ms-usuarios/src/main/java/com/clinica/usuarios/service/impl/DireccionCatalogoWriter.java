package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.model.Direccion;
import com.clinica.usuarios.repository.DireccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Persistencia de direcciones en TX separada para que un fallo por unicidad no aborte
 * la transacción del registro de usuario (PostgreSQL aborta la TX ante el error SQL).
 */
@Component
@RequiredArgsConstructor
public class DireccionCatalogoWriter {

    private final DireccionRepository direccionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Direccion> intentarGuardar(Direccion direccion) {
        try {
            return Optional.of(direccionRepository.save(direccion));
        } catch (DataIntegrityViolationException ex) {
            return Optional.empty();
        }
    }
}
