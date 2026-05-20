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

    private static final int MINUTOS_VALIDEZ = 3;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final VerificationTokenRepository tokenRepository;

    @Override
    @Transactional
    public DatosConfirmacion crearTokenConfirmacion(Usuario usuario) {
        tokenRepository.deleteByUsuario(usuario);
        return guardarNuevoToken(usuario);
    }

    private DatosConfirmacion guardarNuevoToken(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        VerificationToken vToken = VerificationToken.builder()
                .token(token)
                .codigo(codigo)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_VALIDEZ))
                .build();
        tokenRepository.save(vToken);
        return new DatosConfirmacion(token, codigo);
    }
}
