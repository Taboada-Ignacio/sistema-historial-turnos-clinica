package com.clinica.usuarios.config;

import com.clinica.usuarios.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
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
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"No autorizado\", \"message\": \"" + authException.getMessage() + "\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // --- AUTH & REGISTROS ---
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/pacientes/registro/**").permitAll()
                .requestMatchers("/api/administradores/registro/**").permitAll()
                // Aseguramos que cubra tanto "/registro" como "/registro/"
                .requestMatchers("/api/profesionales/registro", "/api/profesionales/registro/**").permitAll()
                
                // --- CONFIRMACIONES Y REENVÍOS ---
                .requestMatchers(HttpMethod.GET, "/api/pacientes/confirmar/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/profesionales/confirmar/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/administradores/confirmar/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/profesionales/reenviar-confirmacion").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/pacientes/reenviar-confirmacion").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/administradores/reenviar-confirmacion").permitAll()

                // --- DATOS MAESTROS (Públicos para los formularios de registro) ---
                .requestMatchers(HttpMethod.GET, "/api/obras-sociales/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/roles/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/especialidades/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/provincias/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/localidades/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/direcciones/**").permitAll()

                // --- RECURSOS ESTÁTICOS ---
                .requestMatchers(HttpMethod.GET, "/fotosPerfilProfesionales/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}