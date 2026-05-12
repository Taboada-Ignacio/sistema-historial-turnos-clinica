package com.clinica.usuarios.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DireccionTextoNormalizerTest {

    @Test
    void nullReturnsEmpty() {
        assertThat(DireccionTextoNormalizer.normalizar(null)).isEmpty();
    }

    @Test
    void trimsAndCollapsesInternalWhitespace() {
        assertThat(DireccionTextoNormalizer.normalizar("  Av.  Siempre   Viva  742  "))
                .isEqualTo("Av. Siempre Viva 742");
    }

    @Test
    void nfcCombiningCharacters() {
        String nfd = "Jose\u0301";
        assertThat(DireccionTextoNormalizer.normalizar(nfd)).isEqualTo("José");
    }
}
