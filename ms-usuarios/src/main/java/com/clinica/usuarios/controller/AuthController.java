package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.AuthRequestDTO;
import com.clinica.usuarios.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO request) {
        try {
            // 1. Spring Security valida email, contraseña y el campo 'enabled' (Estado ACTIVO)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // 2. Si las credenciales son correctas y el usuario está ACTIVO, cargamos los datos
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

            // 3. Generamos el token JWT
            final String jwt = jwtUtil.generateToken(userDetails);

            // 4. Lo devolvemos en formato JSON
            return ResponseEntity.ok(Collections.singletonMap("token", jwt));

        } catch (DisabledException e) {
            // Este catch se dispara cuando enabled = false en UserDetailsServiceImpl
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Cuenta pendiente de activación", 
                                "message", "Por favor, confirmá tu email para poder ingresar al sistema."));

        } catch (BadCredentialsException e) {
            // Credenciales incorrectas
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticación fallida", 
                                "message", "Email o contraseña incorrectos."));

        } catch (Exception e) {
            // Error genérico para no romper el flujo
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno", 
                                "message", "Ocurrió un problema procesando tu solicitud."));
        }
    }
}