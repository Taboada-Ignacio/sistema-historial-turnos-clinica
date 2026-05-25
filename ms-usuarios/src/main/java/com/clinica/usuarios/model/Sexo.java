package com.clinica.usuarios.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Sexo biológico registrado en ficha de usuario (valores permitidos en API y BD).
 */
public enum Sexo {
    MASCULINO,
    FEMENINO;

    @JsonCreator
    public static Sexo fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "MASCULINO", "M" -> MASCULINO;
            case "FEMENINO", "F" -> FEMENINO;
            default -> throw new IllegalArgumentException(
                    "Sexo inválido. Valores permitidos: MASCULINO, FEMENINO (o masculino, femenino).");
        };
    }

    @JsonValue
    public String toJson() {
        return name();
    }

    /** Etiqueta para UI (ej. Masculino). */
    public String getEtiqueta() {
        return switch (this) {
            case MASCULINO -> "Masculino";
            case FEMENINO -> "Femenino";
        };
    }
}
