package com.clinica.usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Este Bean es el que inyectaremos en nuestro Service para encriptar
     * las contraseñas antes de guardarlas en PostgreSQL.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración de la cadena de filtros de seguridad.
     * ATENCIÓN: Por ahora, permitimos todas las peticiones para poder probar
     * el registro desde Postman sin que nos pida un Token JWT.
     * Más adelante aseguraremos las rutas correctamente.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desactivamos CSRF porque las APIs REST con JWT no lo necesitan
            .csrf(AbstractHttpConfigurer::disable)
            // Permitimos el acceso a cualquier endpoint sin autenticación por el momento
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}