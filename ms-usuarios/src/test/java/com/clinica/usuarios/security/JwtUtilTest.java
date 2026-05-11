package com.clinica.usuarios.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias del JWT (claims, validez, expiración) sin levantar todo el contexto.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        SecretKey key = Keys.hmacShaKeyFor(
                "clave-de-prueba-de-exactamente-32-bytes!!".getBytes(StandardCharsets.UTF_8));
        String secretB64 = Base64.getEncoder().encodeToString(key.getEncoded());
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secretB64);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 60_000L);
    }

    @Test
    void generateToken_includesUsernameAndAuthoritiesClaim() {
        UserDetails user = User.builder()
                .username("doc@test.com")
                .password("x")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PROFESIONAL")))
                .build();

        String token = jwtUtil.generateToken(user);

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("doc@test.com");
        Object authorities = jwtUtil.extractClaim(token, claims -> claims.get("authorities"));
        assertThat(authorities).asList().contains("ROLE_PROFESIONAL");
    }

    @Test
    void isTokenValid_returnsTrueForMatchingUserAndFreshToken() {
        UserDetails user = User.builder()
                .username("a@b.com")
                .password("x")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PACIENTE")))
                .build();
        String token = jwtUtil.generateToken(user);

        assertThat(jwtUtil.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForDifferentUser() {
        UserDetails issuer = User.builder()
                .username("one@test.com")
                .password("x")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PACIENTE")))
                .build();
        UserDetails other = User.builder()
                .username("two@test.com")
                .password("x")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PACIENTE")))
                .build();

        String token = jwtUtil.generateToken(issuer);

        assertThat(jwtUtil.isTokenValid(token, other)).isFalse();
    }

    @Test
    void expiredToken_throwsWhenParsingClaims() {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", -60_000L);
        UserDetails user = User.builder()
                .username("exp@test.com")
                .password("x")
                .authorities(List.of())
                .build();
        String token = jwtUtil.generateToken(user);

        assertThatThrownBy(() -> jwtUtil.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
