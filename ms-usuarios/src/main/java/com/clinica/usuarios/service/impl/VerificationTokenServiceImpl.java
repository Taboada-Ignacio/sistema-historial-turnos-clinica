package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.model.Usuario;
import com.clinica.usuarios.model.VerificationToken;
import com.clinica.usuarios.repository.VerificationTokenRepository;
import com.clinica.usuarios.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private static final String CODIGO_SIN_USO_EN_MAIL = "000000";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final VerificationTokenRepository tokenRepository;

    @Override
    @Transactional
    public DatosConfirmacion crearTokenConfirmacion(Usuario usuario) {
        invalidarTokensPrevios(usuario);
        String token = UUID.randomUUID().toString();
        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        guardarToken(usuario, token, codigo);
        return new DatosConfirmacion(token, codigo);
    }

    @Override
    @Transactional
    public String crearTokenActivacionPaciente(Usuario usuario) {
        invalidarTokensPrevios(usuario);
        String token = UUID.randomUUID().toString();
        guardarToken(usuario, token, CODIGO_SIN_USO_EN_MAIL);
        return token;
    }

    @Override
    @Transactional
    public String crearTokenRecuperacionPassword(Usuario usuario) {
        invalidarTokensPrevios(usuario);
        String token = UUID.randomUUID().toString();
        guardarToken(usuario, token, CODIGO_SIN_USO_EN_MAIL);
        return token;
    }

    private void invalidarTokensPrevios(Usuario usuario) {
        if (usuario.getIdUsuario() != null) {
            tokenRepository.deleteByUsuario_IdUsuario(usuario.getIdUsuario());
        } else {
            tokenRepository.deleteByUsuario(usuario);
        }
    }

    private void guardarToken(Usuario usuario, String token, String codigo) {
        VerificationToken vToken = VerificationToken.builder()
                .token(token)
                .codigo(codigo)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusHours(HORAS_VALIDEZ))
                .build();
        tokenRepository.save(vToken);
    }
}
