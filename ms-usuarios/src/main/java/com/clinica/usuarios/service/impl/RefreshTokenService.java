package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.model.RefreshToken;
import com.clinica.usuarios.model.Usuario;
import com.clinica.usuarios.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refreshExpiration:604800000}") // default 7 days in ms
    private long refreshExpirationMs;

    public RefreshToken createRefreshToken(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        RefreshToken rt = RefreshToken.builder()
                .token(token)
                .usuario(usuario)
                .createdAt(now)
                .expiryDate(now.plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(rt);
    }

    public RefreshToken verifyAndRotate(String token) {
        RefreshToken existing = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Refresh token inválido"));

        if (existing.isRevoked() || existing.getExpiryDate().isBefore(Instant.now())) {
            // revoke to be safe
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
            throw new RecursoNoEncontradoException("Refresh token inválido o expirado");
        }

        // Revoke the old token and create a new one (rotation)
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return createRefreshToken(existing.getUsuario());
    }

    public void revokeAllForUser(Usuario usuario) {
        refreshTokenRepository.deleteByUsuario(usuario);
    }
}

