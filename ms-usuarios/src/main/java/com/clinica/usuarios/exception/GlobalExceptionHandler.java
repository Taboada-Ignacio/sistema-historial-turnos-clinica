package com.clinica.usuarios.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- MANEJO DE EXCEPCIONES DE NEGOCIO ---

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<Map<String, String>> handleReglaDeNegocio(ReglaDeNegocioException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        if (ex.getCode() != null && !ex.getCode().isBlank()) {
            error.put("code", ex.getCode());
        }
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // --- NUEVO: MANEJO DE EXCEPCIONES DE SEGURIDAD (SPRING SECURITY) ---

    /**
     * Se dispara cuando el usuario existe pero 'enabled' es false (Estado PENDIENTE).
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, String>> handleDisabledException(DisabledException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Cuenta no activada");
        error.put("mensaje", "Por favor, confirmá tu cuenta a través del correo electrónico enviado.");
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN); // 403 Forbidden
    }

    /**
     * Se dispara cuando el email o la contraseña son incorrectos.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Credenciales inválidas");
        error.put("mensaje", "El correo o la contraseña ingresados no son correctos.");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
    }

    // --- EXCEPCIÓN DE VALIDACIÓN DE DTOs ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String nombreCampo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(nombreCampo, mensaje);
        });
        
        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST);
    }
}