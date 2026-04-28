package service;

import model.Admin;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SessionManager.
 * Cubre ciclo de vida de la sesión, checkAuth y flags de estado.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SessionManagerTest {

    private static final UUID ADMIN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @BeforeEach
    void resetarEstado() throws Exception {
        // Resetear todo el estado estático antes de cada test
        SessionManager.cerrarSesion();
        setField("appAutorizada", false);
        setField("modoDemo", false);
    }

    // ─────────────────────────────────────────────────────────────
    // iniciarSesion / cerrarSesion
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("iniciarSesion: admin válido → haySesion() true")
    void iniciarSesion_adminValido_haySession() {
        SessionManager.iniciarSesion(adminConToken("token-abc"));
        assertTrue(SessionManager.haySesion());
    }

    @Test
    @DisplayName("cerrarSesion: tras iniciar → haySesion() false")
    void cerrarSesion_trasSesionActiva_noHaySesion() {
        SessionManager.iniciarSesion(adminConToken("token-abc"));
        SessionManager.cerrarSesion();
        assertFalse(SessionManager.haySesion());
    }

    @Test
    @DisplayName("getAdmin: retorna el admin de la sesión activa")
    void getAdmin_conSesionActiva_retornaAdmin() {
        Admin admin = adminConToken("token-xyz");
        SessionManager.iniciarSesion(admin);
        assertEquals(admin, SessionManager.getAdmin());
    }

    @Test
    @DisplayName("getAdmin: sin sesión → null")
    void getAdmin_sinSesion_retornaNull() {
        assertNull(SessionManager.getAdmin());
    }

    @Test
    @DisplayName("getToken: con sesión activa → retorna el access token")
    void getToken_conSesion_retornaToken() {
        SessionManager.iniciarSesion(adminConToken("mi-token"));
        assertEquals("mi-token", SessionManager.getToken());
    }

    @Test
    @DisplayName("getToken: sin sesión → null")
    void getToken_sinSesion_retornaNull() {
        assertNull(SessionManager.getToken());
    }

    @Test
    @DisplayName("haySesion: admin sin token → false")
    void haySesion_adminSinToken_retornaFalse() {
        Admin admin = new Admin();
        admin.setId(ADMIN_ID);
        admin.setEmail("test@taxi.com");
        // No setAccessToken → null
        SessionManager.iniciarSesion(admin);
        assertFalse(SessionManager.haySesion());
    }

    // ─────────────────────────────────────────────────────────────
    // checkAuth()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("checkAuth: app no autorizada → SecurityException")
    void checkAuth_appNoAutorizada_lanzaExcepcion() {
        assertThrows(SecurityException.class, SessionManager::checkAuth);
    }

    @Test
    @DisplayName("checkAuth: app autorizada pero sin sesión → SecurityException")
    void checkAuth_appAutorizadaSinSesion_lanzaExcepcion() throws Exception {
        setField("appAutorizada", true);
        assertThrows(SecurityException.class, SessionManager::checkAuth);
    }

    @Test
    @DisplayName("checkAuth: app autorizada y con sesión → no lanza excepción")
    void checkAuth_appAutorizadaConSesion_noLanzaExcepcion() throws Exception {
        setField("appAutorizada", true);
        SessionManager.iniciarSesion(adminConToken("token-ok"));
        assertDoesNotThrow(SessionManager::checkAuth);
    }

    // ─────────────────────────────────────────────────────────────
    // inicializarAcceso()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("inicializarAcceso: apiKey null → modo demo activado")
    void inicializarAcceso_keyNula_activaModoDemo() {
        boolean resultado = SessionManager.inicializarAcceso(null);
        assertTrue(resultado);
        assertTrue(SessionManager.isModoDemo());
        assertTrue(SessionManager.isAppAutorizada());
    }

    @Test
    @DisplayName("inicializarAcceso: apiKey vacía → modo demo activado")
    void inicializarAcceso_keyVacia_activaModoDemo() {
        boolean resultado = SessionManager.inicializarAcceso("   ");
        assertTrue(resultado);
        assertTrue(SessionManager.isModoDemo());
    }

    // ─────────────────────────────────────────────────────────────
    // Estado ventana
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizarEstadoVentana: flags se actualizan correctamente")
    void actualizarEstadoVentana_actualizaFlags() {
        SessionManager.actualizarEstadoVentana(true, false);
        assertTrue(SessionManager.isVentanaMaximizada());
        assertFalse(SessionManager.isPantallaCompleta());

        SessionManager.actualizarEstadoVentana(false, true);
        assertFalse(SessionManager.isVentanaMaximizada());
        assertTrue(SessionManager.isPantallaCompleta());
    }

    @Test
    @DisplayName("iniciarSesion: resetea los flags de ventana")
    void iniciarSesion_resetaFlagsVentana() {
        SessionManager.actualizarEstadoVentana(true, true);
        SessionManager.iniciarSesion(adminConToken("tk"));
        assertFalse(SessionManager.isVentanaMaximizada());
        assertFalse(SessionManager.isPantallaCompleta());
    }

    @Test
    @DisplayName("cerrarSesion: resetea los flags de ventana")
    void cerrarSesion_resetaFlagsVentana() {
        SessionManager.iniciarSesion(adminConToken("tk"));
        SessionManager.actualizarEstadoVentana(true, true);
        SessionManager.cerrarSesion();
        assertFalse(SessionManager.isVentanaMaximizada());
        assertFalse(SessionManager.isPantallaCompleta());
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private Admin adminConToken(String token) {
        Admin admin = new Admin();
        admin.setId(ADMIN_ID);
        admin.setEmail("test@taxi.com");
        admin.setAccessToken(token);
        return admin;
    }

    private void setField(String name, Object value) throws Exception {
        Field f = SessionManager.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }
}