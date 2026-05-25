package com.clinica.usuarios.exception;

import lombok.Getter;

@Getter
public class ReglaDeNegocioException extends RuntimeException {

    private final String code;

    public ReglaDeNegocioException(String message) {
        this(message, null);
    }

    public ReglaDeNegocioException(String message, String code) {
        super(message);
        this.code = code;
    }
}
