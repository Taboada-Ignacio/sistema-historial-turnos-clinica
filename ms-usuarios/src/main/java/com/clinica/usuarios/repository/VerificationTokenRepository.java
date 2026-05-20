package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.VerificationToken;
import com.clinica.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    
    // Para validar el token cuando el usuario hace clic en el mail
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUsuario_EmailAndCodigo(String email, String codigo);

    void deleteByUsuario(Usuario usuario);
}