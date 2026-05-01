package com.clinica.usuarios.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- TUS EXCEPCIONES ORIGINALES ---

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<Map<String, String>> handleReglaDeNegocio(ReglaDeNegocioException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // --- NUEVA: EXCEPCIÓN DE VALIDACIÓN DE DTOs (@Valid, @Pattern, etc.) ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        
        // Iteramos sobre todos los errores de validación que saltaron
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String nombreCampo = ((FieldError) error).getField(); // ej: "password" o "nombre"
            String mensaje = error.getDefaultMessage();           // ej: "La contraseña debe contener..."
            
            // Guardamos el campo y su error específico
            errores.put(nombreCampo, mensaje);
        });
        
        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST);
    }
}