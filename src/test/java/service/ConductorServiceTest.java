package service;

import model.Conductor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ConductorRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ConductorService.
 * Se mockean ConductorRepository y SessionManager para aislar la lógica de negocio.
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ConductorServiceTest {

    @Mock
    private ConductorRepository repoMock;

    private ConductorService service;

    // Admin de prueba reutilizable
    private static final UUID ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() throws Exception {
        // Inyectamos el mock del repositorio via reflexión (el campo es privado)
        service = new ConductorService();
        Field repoField = ConductorService.class.getDeclaredField("repo");
        repoField.setAccessible(true);
        repoField.set(service, repoMock);

        // Simulamos sesión activa por defecto
        mockSession(true);
    }

    @AfterEach
    void tearDown() {
        // Limpiar el estado estático de SessionManager entre tests
        SessionManager.cerrarSesion();
    }

    // ─────────────────────────────────────────────────────────────
    // registrar()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("registrar: matrícula válida y nombre correcto → true")
    void registrar_datosValidos_retornaTrue() {
        Conductor c = conductorValido("1234ABC", "Joan Puig");
        when(repoMock.existeMatricula("1234ABC", c.getCond_admin())).thenReturn(false);
        assertTrue(service.registrar(c));
        verify(repoMock).guardar(c);
    }

    @Test
    @DisplayName("registrar: matrícula con letras minúsculas → false")
    void registrar_matriculaMinusculas_retornaFalse() {
        Conductor c = conductorValido("1234abc", "Joan Puig");
        assertFalse(service.registrar(c));
        verify(repoMock, never()).guardar(any());
    }

    @Test
    @DisplayName("registrar: matrícula con sólo 3 dígitos → false")
    void registrar_matriculaTresDigitos_retornaFalse() {
        Conductor c = conductorValido("123ABC", "Joan Puig");
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: matrícula con 5 dígitos → false")
    void registrar_matriculaCincoDigitos_retornaFalse() {
        Conductor c = conductorValido("12345AB", "Joan Puig");
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: matrícula con 2 letras → false")
    void registrar_matriculaDosLetras_retornaFalse() {
        Conductor c = conductorValido("1234AB", "Joan Puig");
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: matrícula null → false")
    void registrar_matriculaNula_retornaFalse() {
        Conductor c = conductorValido(null, "Joan Puig");
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: nombre vacío → false")
    void registrar_nombreVacio_retornaFalse() {
        Conductor c = conductorValido("1234ABC", "   ");
        assertFalse(service.registrar(c));
        verify(repoMock, never()).guardar(any());
    }

    @Test
    @DisplayName("registrar: nombre null → false")
    void registrar_nombreNulo_retornaFalse() {
        Conductor c = conductorValido("1234ABC", null);
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: matrícula duplicada → false")
    void registrar_matriculaDuplicada_retornaFalse() {
        Conductor c = conductorValido("1234ABC", "Joan Puig");
        when(repoMock.existeMatricula("1234ABC", c.getCond_admin())).thenReturn(true);
        assertFalse(service.registrar(c));
        verify(repoMock, never()).guardar(any());
    }

    @Test
    @DisplayName("registrar: matrícula formato límite válido 0000AAA → true")
    void registrar_matriculaLimiteInferior_retornaTrue() {
        Conductor c = conductorValido("0000AAA", "Test");
        when(repoMock.existeMatricula("0000AAA", c.getCond_admin())).thenReturn(false);
        assertTrue(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: matrícula formato límite válido 9999ZZZ → true")
    void registrar_matriculaLimiteSuperior_retornaTrue() {
        Conductor c = conductorValido("9999ZZZ", "Test");
        when(repoMock.existeMatricula("9999ZZZ", c.getCond_admin())).thenReturn(false);
        assertTrue(service.registrar(c));
}

    @Test
    @DisplayName("registrar: sin sesión activa → false")
    void registrar_sinSesion_retornaFalse() {
        mockSession(false);
        Conductor c = conductorValido("1234ABC", "Joan Puig");
        assertFalse(service.registrar(c));
        verify(repoMock, never()).guardar(any());
    }

    // ─────────────────────────────────────────────────────────────
    // editar()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("editar: nombre válido → delega en repo y retorna true")
    void editar_nombreValido_retornaTrue() {
        when(repoMock.actualizarNombre(1, "Nou Nom")).thenReturn(true);
        assertTrue(service.editar(1, "Nou Nom"));
    }

    @Test
    @DisplayName("editar: nombre con espacios extra → trim y delega")
    void editar_nombreConEspacios_retornaTrueYTrimea() {
        when(repoMock.actualizarNombre(1, "Joan Puig")).thenReturn(true);
        assertTrue(service.editar(1, "  Joan Puig  "));
        verify(repoMock).actualizarNombre(1, "Joan Puig");
    }

    @Test
    @DisplayName("editar: nombre vacío → false sin llamar al repo")
    void editar_nombreVacio_retornaFalse() {
        assertFalse(service.editar(1, "   "));
        verify(repoMock, never()).actualizarNombre(anyInt(), anyString());
    }

    @Test
    @DisplayName("editar: nombre null → false")
    void editar_nombreNulo_retornaFalse() {
        assertFalse(service.editar(1, null));
    }

    @Test
    @DisplayName("editar: sin sesión → false")
    void editar_sinSesion_retornaFalse() {
        mockSession(false);
        assertFalse(service.editar(1, "Nom Valid"));
        verify(repoMock, never()).actualizarNombre(anyInt(), anyString());
    }

    // ─────────────────────────────────────────────────────────────
    // eliminar()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: id existente → true")
    void eliminar_idExistente_retornaTrue() {
        when(repoMock.eliminar(5)).thenReturn(true);
        assertTrue(service.eliminar(5));
    }

    @Test
    @DisplayName("eliminar: id inexistente → false")
    void eliminar_idInexistente_retornaFalse() {
        when(repoMock.eliminar(99)).thenReturn(false);
        assertFalse(service.eliminar(99));
    }

    @Test
    @DisplayName("eliminar: sin sesión → false")
    void eliminar_sinSesion_retornaFalse() {
        mockSession(false);
        assertFalse(service.eliminar(1));
        verify(repoMock, never()).eliminar(anyInt());
    }

    // ─────────────────────────────────────────────────────────────
    // listarTodos()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarTodos: con sesión → delega al repo con adminId")
    void listarTodos_conSesion_delegaRepo() {
        List<Conductor> lista = List.of(conductorValido("1234ABC", "Joan"));
        when(repoMock.findAllByAdminId(ADMIN_ID)).thenReturn(lista);

        List<Conductor> resultado = service.listarTodos();
        assertEquals(1, resultado.size());
        verify(repoMock).findAllByAdminId(ADMIN_ID);
    }

    @Test
    @DisplayName("listarTodos: sin sesión → SecurityException")
    void listarTodos_sinSesion_lanzaExcepcion() {
        mockSession(false);
        assertThrows(SecurityException.class, () -> service.listarTodos());
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private Conductor conductorValido(String matricula, String nombre) {
        Conductor c = new Conductor();
        c.setMatricula(matricula);
        c.setNombre(nombre);
        c.setCond_admin(ADMIN_ID);
        return c;
    }

    private void mockSession(boolean activa) {
        SessionManager.cerrarSesion();
        if (activa) {
            // Forzamos appAutorizada via inicializarAcceso con key nula = modoDemo
            // y luego iniciamos sesión con un admin de prueba
            try {
                Field appAuth = SessionManager.class.getDeclaredField("appAutorizada");
                appAuth.setAccessible(true);
                appAuth.set(null, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            model.Admin admin = new model.Admin();
            admin.setId(ADMIN_ID);
            admin.setEmail("test@taxi.com");
            admin.setAccessToken("fake-token-test");
            SessionManager.iniciarSesion(admin);
        }
    }
}