package com.clinica.usuarios.controller;

import com.clinica.usuarios.dto.request.AuthRequestDTO;
import com.clinica.usuarios.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO request) {
        
        // 1. Spring Security valida el email y la contraseña contra la Base de Datos
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Si las credenciales son correctas, cargamos el usuario
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // 3. Generamos el token JWT
        final String jwt = jwtUtil.generateToken(userDetails);

        // 4. Lo devolvemos en formato JSON
        return ResponseEntity.ok(Collections.singletonMap("token", jwt));
    }
}