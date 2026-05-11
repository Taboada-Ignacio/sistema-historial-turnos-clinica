package com.clinica.usuarios.constants;

/**
 * Valor único en catálogos para filas reservadas (reasignación al borrar sin romper FK).
 */
public final class CatalogoSentinelConstants {

    public static final String SIN_ESPECIFICAR = "SIN ESPECIFICAR";

    private CatalogoSentinelConstants() {
    }

    public static boolean esSentinelNombreODescripcion(String valor) {
        return valor != null && SIN_ESPECIFICAR.equalsIgnoreCase(valor.trim());
    }
}
