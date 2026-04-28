package model;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el modelo Viaje.
 * Cubre la lógica de negocio del modelo: cruzaMedianoche().
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ViajeModelTest {

    @Test
    @DisplayName("constructor: genera UUID automáticamente")
    void constructor_generaUUIDautomatico() {
        Viaje v = new Viaje();
        assertNotNull(v.getId(), "El UUID debe generarse en el constructor");
    }

    @Test
    @DisplayName("constructor: dos instancias tienen UUIDs distintos")
    void constructor_dosInstancias_uuidsDistintos() {
        Viaje v1 = new Viaje();
        Viaje v2 = new Viaje();
        assertNotEquals(v1.getId(), v2.getId());
    }

    @Test
    @DisplayName("cruzaMedianoche: diaFin posterior a dia → true")
    void cruzaMedianoche_diaFinPosterior_retornaTrue() {
        Viaje v = new Viaje();
        v.setDia(LocalDate.of(2025, 6, 15));
        v.setDiaFin(LocalDate.of(2025, 6, 16));
        assertTrue(v.cruzaMedianoche());
    }

    @Test
    @DisplayName("cruzaMedianoche: diaFin igual a dia → false")
    void cruzaMedianoche_diaFinIgual_retornaFalse() {
        Viaje v = new Viaje();
        v.setDia(LocalDate.of(2025, 6, 15));
        v.setDiaFin(LocalDate.of(2025, 6, 15));
        assertFalse(v.cruzaMedianoche());
    }

    @Test
    @DisplayName("cruzaMedianoche: diaFin null → false (sin NPE)")
    void cruzaMedianoche_diaFinNulo_retornaFalse() {
        Viaje v = new Viaje();
        v.setDia(LocalDate.of(2025, 6, 15));
        v.setDiaFin(null);
        assertFalse(v.cruzaMedianoche());
    }

    @Test
    @DisplayName("cruzaMedianoche: diaFin anterior a dia → false (fecha incorrecta)")
    void cruzaMedianoche_diaFinAnterior_retornaFalse() {
        Viaje v = new Viaje();
        v.setDia(LocalDate.of(2025, 6, 15));
        v.setDiaFin(LocalDate.of(2025, 6, 14));
        assertFalse(v.cruzaMedianoche());
    }

    @Test
    @DisplayName("cruzaMedianoche: cruce de mes → true")
    void cruzaMedianoche_cruceDeMes_retornaTrue() {
        Viaje v = new Viaje();
        v.setDia(LocalDate.of(2025, 1, 31));
        v.setDiaFin(LocalDate.of(2025, 2, 1));
        assertTrue(v.cruzaMedianoche());
    }

    @Test
    @DisplayName("cruzaMedianoche: cruce de año → true")
    void cruzaMedianoche_cruceDeAnio_retornaTrue() {
        Viaje v = new Viaje();
        v.setDia(LocalDate.of(2024, 12, 31));
        v.setDiaFin(LocalDate.of(2025, 1, 1));
        assertTrue(v.cruzaMedianoche());
    }

    @Test
    @DisplayName("setId: permite sobreescribir el UUID generado")
    void setId_sobrescribeUUID() {
        Viaje v = new Viaje();
        UUID nuevo = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        v.setId(nuevo);
        assertEquals(nuevo, v.getId());
    }

    @Test
    @DisplayName("getters/setters: propiedades básicas del viaje")
    void gettersSetters_propiedadesBasicas() {
        Viaje v = new Viaje();
        LocalDate dia  = LocalDate.of(2025, 6, 20);
        LocalTime hora = LocalTime.of(9, 30);

        v.setDia(dia);
        v.setHora(hora);
        v.setPuntorecogida("Plaça Catalunya");
        v.setPuntodejada("Aeroport BCN");
        v.setTelefonocliente("612000000");

        assertEquals(dia, v.getDia());
        assertEquals(hora, v.getHora());
        assertEquals("Plaça Catalunya", v.getPuntorecogida());
        assertEquals("Aeroport BCN", v.getPuntodejada());
        assertEquals("612000000", v.getTelefonocliente());
    }
}