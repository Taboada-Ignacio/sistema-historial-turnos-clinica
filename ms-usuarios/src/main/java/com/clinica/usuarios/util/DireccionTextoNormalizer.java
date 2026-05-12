package com.clinica.usuarios.util;

import java.text.Normalizer;

/**
 * Normaliza el texto de dirección para búsqueda y persistencia consistente
 * (trim, espacios internos colapsados, forma Unicode NFC).
 */
public final class DireccionTextoNormalizer {

    private DireccionTextoNormalizer() {
    }

    public static String normalizar(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim().replaceAll("\\s+", " ");
        return Normalizer.normalize(trimmed, Normalizer.Form.NFC);
    }
}
