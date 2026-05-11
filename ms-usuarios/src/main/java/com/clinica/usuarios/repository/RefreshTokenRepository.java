package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.RefreshToken;
import com.clinica.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUsuario(Usuario usuario);
    void deleteByUsuario(Usuario usuario);
}

