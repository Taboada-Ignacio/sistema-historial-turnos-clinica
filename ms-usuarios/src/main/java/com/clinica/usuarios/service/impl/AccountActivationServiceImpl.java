package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.model.Estado;
import com.clinica.usuarios.model.Usuario;
import com.clinica.usuarios.model.VerificationToken;
import com.clinica.usuarios.repository.EstadoRepository;
import com.clinica.usuarios.repository.UsuarioRepository;
import com.clinica.usuarios.repository.VerificationTokenRepository;
import com.clinica.usuarios.service.AccountActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AccountActivationServiceImpl implements AccountActivationService {

    private final VerificationTokenRepository tokenRepository;
    private final EstadoRepository estadoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void confirmarCuentaDesdeToken(VerificationToken verificationToken, Consumer<Usuario> registrarHistorialEstado) {
        if (verificationToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new ReglaDeNegocioException(
                    "El código o el enlace de confirmación ha expirado (3 minutos). Solicitá uno nuevo.");
        }

        Usuario usuario = verificationToken.getUsuario();
        Estado estadoActivo = estadoRepository.findByNombre("ACTIVO")
                .orElseThrow(() -> new ReglaDeNegocioException("Estado ACTIVO no disponible"));

        usuario.setEstadoActual(estadoActivo);
        usuarioRepository.save(usuario);
        registrarHistorialEstado.accept(usuario);
        tokenRepository.delete(verificationToken);
    }
}
