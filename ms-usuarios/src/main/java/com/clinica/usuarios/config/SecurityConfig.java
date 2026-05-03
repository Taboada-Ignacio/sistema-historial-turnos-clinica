package com.clinica.usuarios.config;

import com.clinica.usuarios.security.JwtAuthFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // Agregamos manejo de excepciones explícito para devolver 401 en vez de 403 genérico
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"No autorizado\", \"message\": \"" + authException.getMessage() + "\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Usamos patrones con asteriscos para cubrir sub-rutas
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/pacientes/registro/**").permitAll()
                .requestMatchers("/api/administradores/registro/**").permitAll()
                .requestMatchers("/api/profesionales/registro/**").permitAll()
                
                .requestMatchers(HttpMethod.GET, "/api/pacientes/confirmar/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/profesionales/confirmar/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/administradores/confirmar/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/obras-sociales/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/roles/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/especialidades/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/provincias/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/localidades/**").permitAll()

                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}