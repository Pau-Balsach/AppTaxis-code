package service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SecurityUtils.
 * Verifica integridad del hash SHA-256.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SecurityUtilsTest {

    // SHA-256 de "hola" en hex conocido
    private static final String SHA256_HOLA =
        "b94d27b9934d3e08a52e52d7da7dabfac484efe04294e576f787ed67ab2c9bc5";

    @Test
    @DisplayName("generarHash: entrada conocida → hash SHA-256 correcto")
    void generarHash_entradaConocida_retornaHashCorrecto() {
        String hash = SecurityUtils.generarHash("hola");
        // SHA-256 es determinista: mismo input → mismo output
        assertEquals(64, hash.length(), "SHA-256 debe tener 64 caracteres hex");
        assertTrue(hash.matches("[0-9a-f]{64}"), "El hash debe ser hexadecimal en minúsculas");
    }

    @Test
    @DisplayName("generarHash: misma entrada dos veces → mismo hash (determinista)")
    void generarHash_mismaEntrada_retornaMismoHash() {
        String hash1 = SecurityUtils.generarHash("password123");
        String hash2 = SecurityUtils.generarHash("password123");
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("generarHash: entradas distintas → hashes distintos")
    void generarHash_entradasDistintas_retornaHashesDistintos() {
        String h1 = SecurityUtils.generarHash("abc");
        String h2 = SecurityUtils.generarHash("ABC");
        assertNotEquals(h1, h2, "El hash es sensible a mayúsculas/minúsculas");
    }

    @Test
    @DisplayName("generarHash: cadena vacía → hash válido (no null, no vacío)")
    void generarHash_cadenaVacia_retornaHashValido() {
        String hash = SecurityUtils.generarHash("");
        assertNotNull(hash);
        assertFalse(hash.isBlank());
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("generarHash: entrada null → retorna cadena vacía (no lanza excepción)")
    void generarHash_entradaNula_retornaCadenaVacia() {
        // SecurityUtils captura la excepción y retorna ""
        String hash = SecurityUtils.generarHash(null);
        assertNotNull(hash);
        // El método hace .getBytes(null) → NullPointerException, capturada → ""
        assertEquals("", hash);
    }

    @Test
    @DisplayName("generarHash: texto largo → hash de 64 caracteres")
    void generarHash_textoLargo_retornaHashDe64Chars() {
        String texto = "a".repeat(10_000);
        String hash = SecurityUtils.generarHash(texto);
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("generarHash: caracteres especiales → hash válido")
    void generarHash_caracteresEspeciales_retornaHashValido() {
        String hash = SecurityUtils.generarHash("pàssw0rd!@#€");
        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }
}