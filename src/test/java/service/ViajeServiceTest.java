package service;

import model.Admin;
import model.Conductor;
import model.Viaje;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ViajeRepository;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ViajeService.
 * Cubre creación, edición, eliminación y consultas con control de sesión.
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ViajeServiceTest {

    @Mock
    private ViajeRepository repoMock;

    private ViajeService service;

    private static final UUID ADMIN_ID  = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID VIAJE_ID  = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        service = new ViajeService();
        Field repoField = ViajeService.class.getDeclaredField("repo");
        repoField.setAccessible(true);
        repoField.set(service, repoMock);
        mockSession(true);
    }

    @AfterEach
    void tearDown() {
        SessionManager.cerrarSesion();
    }

    // ─────────────────────────────────────────────────────────────
    // crear()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: viaje válido con conductorId → true")
    void crear_viajeValido_retornaTrue() {
        Viaje v = viajeBasico();
        doNothing().when(repoMock).guardar(v, 1);
        assertTrue(service.crear(v, 1));
        verify(repoMock).guardar(v, 1);
    }

    @Test
    @DisplayName("crear: conductor no existe → IllegalArgumentException → false")
    void crear_conductorNoExiste_retornaFalse() {
        Viaje v = viajeBasico();
        doThrow(new IllegalArgumentException("Conductor no encontrado: 99"))
            .when(repoMock).guardar(v, 99);
        assertFalse(service.crear(v, 99));
    }

    @Test
    @DisplayName("crear: error inesperado en repo → false")
    void crear_errorRepo_retornaFalse() {
        Viaje v = viajeBasico();
        doThrow(new RuntimeException("DB error")).when(repoMock).guardar(v, 1);
        assertFalse(service.crear(v, 1));
    }

    @Test
    @DisplayName("crear: sin sesión activa → false")
    void crear_sinSesion_retornaFalse() {
        mockSession(false);
        assertFalse(service.crear(viajeBasico(), 1));
        verify(repoMock, never()).guardar(any(), anyInt());
    }

    // ─────────────────────────────────────────────────────────────
    // editar()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("editar: viaje existente → true")
    void editar_viajeExistente_retornaTrue() {
        Viaje v = viajeBasico();
        when(repoMock.actualizar(v)).thenReturn(true);
        assertTrue(service.editar(v));
    }

    @Test
    @DisplayName("editar: viaje no existe → false")
    void editar_viajeInexistente_retornaFalse() {
        Viaje v = viajeBasico();
        when(repoMock.actualizar(v)).thenReturn(false);
        assertFalse(service.editar(v));
    }

    @Test
    @DisplayName("editar: sin sesión → false")
    void editar_sinSesion_retornaFalse() {
        mockSession(false);
        assertFalse(service.editar(viajeBasico()));
        verify(repoMock, never()).actualizar(any());
    }

    // ─────────────────────────────────────────────────────────────
    // eliminar()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: id válido existente → true")
    void eliminar_idExistente_retornaTrue() {
        when(repoMock.eliminar(VIAJE_ID)).thenReturn(true);
        assertTrue(service.eliminar(VIAJE_ID));
    }

    @Test
    @DisplayName("eliminar: id no existente → false")
    void eliminar_idInexistente_retornaFalse() {
        UUID idFalso = UUID.randomUUID();
        when(repoMock.eliminar(idFalso)).thenReturn(false);
        assertFalse(service.eliminar(idFalso));
    }

    @Test
    @DisplayName("eliminar: sin sesión → false")
    void eliminar_sinSesion_retornaFalse() {
        mockSession(false);
        assertFalse(service.eliminar(VIAJE_ID));
        verify(repoMock, never()).eliminar(any());
    }

    // ─────────────────────────────────────────────────────────────
    // listarPorMes()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarPorMes: mes con viajes → lista no vacía")
    void listarPorMes_conViajes_retornaLista() {
        List<Viaje> viajes = List.of(viajeBasico(), viajeBasico());
        when(repoMock.findByMes(2025, 6, ADMIN_ID)).thenReturn(viajes);

        List<Viaje> resultado = service.listarPorMes(2025, 6);
        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("listarPorMes: mes sin viajes → lista vacía")
    void listarPorMes_sinViajes_retornaVacio() {
        when(repoMock.findByMes(2025, 1, ADMIN_ID)).thenReturn(List.of());
        assertTrue(service.listarPorMes(2025, 1).isEmpty());
    }

    @Test
    @DisplayName("listarPorMes: sin sesión → SecurityException")
    void listarPorMes_sinSesion_lanzaExcepcion() {
        mockSession(false);
        assertThrows(SecurityException.class, () -> service.listarPorMes(2025, 6));
    }

    // ─────────────────────────────────────────────────────────────
    // listarPorFecha()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarPorFecha: conductor con viajes ese día → lista correcta")
    void listarPorFecha_conViajes_retornaLista() {
        LocalDate hoy = LocalDate.now();
        List<Viaje> viajes = List.of(viajeBasico());
        when(repoMock.findByConductorAndFecha(1, hoy, ADMIN_ID)).thenReturn(viajes);

        assertEquals(1, service.listarPorFecha(1, hoy).size());
    }

    // ─────────────────────────────────────────────────────────────
    // listarPorConductor()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarPorConductor: retorna viajes del conductor indicado")
    void listarPorConductor_retornaViajesConductor() {
        List<Viaje> viajes = List.of(viajeBasico());
        when(repoMock.findByConductor(3, ADMIN_ID)).thenReturn(viajes);

        List<Viaje> resultado = service.listarPorConductor(3);
        assertEquals(1, resultado.size());
        verify(repoMock).findByConductor(3, ADMIN_ID);
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private Viaje viajeBasico() {
        Viaje v = new Viaje();
        v.setId(VIAJE_ID);
        v.setDia(LocalDate.now());
        v.setHora(LocalTime.of(10, 0));
        v.setPuntorecogida("Carrer Major 1");
        v.setPuntodejada("Aeroport T1");
        v.setTelefonocliente("612345678");

        Conductor c = new Conductor();
        c.setId(1);
        c.setCond_admin(ADMIN_ID);
        v.setConductor(c);
        return v;
    }

    private void mockSession(boolean activa) {
        SessionManager.cerrarSesion();
        if (activa) {
            try {
                Field f = SessionManager.class.getDeclaredField("appAutorizada");
                f.setAccessible(true);
                f.set(null, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Admin admin = new Admin();
            admin.setId(ADMIN_ID);
            admin.setEmail("admin@taxi.com");
            admin.setAccessToken("token-test");
            SessionManager.iniciarSesion(admin);
        }
    }
}