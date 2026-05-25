package com.clinica.usuarios.config;



import com.clinica.usuarios.security.JwtAuthFilter;

import com.clinica.usuarios.security.PublicRequestPaths;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;



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



    /**

     * Misma lógica que {@link PublicRequestPaths#shouldBypassJwtFilter(HttpServletRequest)}:

     * los matchers MVC de Spring Security no estaban aplicando {@code permitAll} en POST /registro.

     */

    private static boolean isPublicApiRequest(HttpServletRequest request) {

        return PublicRequestPaths.shouldBypassJwtFilter(request);

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

                // Onboarding admin (alta + confirmación): público; X-System-Key solo en POST /registro.
                .requestMatchers(
                        new AntPathRequestMatcher("/api/onboarding/admin/**"),
                        new AntPathRequestMatcher("/usuarios/api/onboarding/admin/**")
                ).permitAll()

                .requestMatchers(SecurityConfig::isPublicApiRequest).permitAll()

                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                .anyRequest().authenticated()

            )

            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);



        return http.build();

    }

}


