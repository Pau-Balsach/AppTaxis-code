package service;

import model.Cliente;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ClienteRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ClienteService.
 * Cubre validaciones de teléfono español y email.
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repoMock;

    private ClienteService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ClienteService();
        Field repoField = ClienteService.class.getDeclaredField("repo");
        repoField.setAccessible(true);
        repoField.set(service, repoMock);
    }

    // ─────────────────────────────────────────────────────────────
    // registrar()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("registrar: datos válidos completos → true")
    void registrar_datosValidos_retornaTrue() {
        Cliente c = clienteValido("Maria Garcia", "612345678", "maria@email.com");
        assertTrue(service.registrar(c));
        verify(repoMock).guardar(c);
    }

    // --- Validaciones nombre ---

    @Test
    @DisplayName("registrar: nombre null → false")
    void registrar_nombreNulo_retornaFalse() {
        Cliente c = clienteValido(null, "612345678", "a@b.com");
        assertFalse(service.registrar(c));
        verify(repoMock, never()).guardar(any());
    }

    @Test
    @DisplayName("registrar: nombre vacío → false")
    void registrar_nombreVacio_retornaFalse() {
        Cliente c = clienteValido("   ", "612345678", "a@b.com");
        assertFalse(service.registrar(c));
    }

    // --- Validaciones teléfono español ---

    @Test
    @DisplayName("registrar: teléfono móvil 6xx válido → true")
    void registrar_telefonoMovil6_retornaTrue() {
        Cliente c = clienteValido("Test", "612345678", "a@b.com");
        assertTrue(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: teléfono móvil 7xx válido → true")
    void registrar_telefonoMovil7_retornaTrue() {
        Cliente c = clienteValido("Test", "712345678", "a@b.com");
        assertTrue(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: teléfono fijo 8xx válido → true")
    void registrar_telefonoFijo8_retornaTrue() {
        Cliente c = clienteValido("Test", "812345678", "a@b.com");
        assertTrue(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: teléfono fijo 9xx válido → true")
    void registrar_telefonoFijo9_retornaTrue() {
        Cliente c = clienteValido("Test", "912345678", "a@b.com");
        assertTrue(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: teléfono con prefijo +34 válido → true")
    void registrar_telefonoConPrefijo34_retornaTrue() {
        Cliente c = clienteValido("Test", "+34612345678", "a@b.com");
        assertTrue(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: teléfono empieza por 5 → false")
    void registrar_telefonoCon5_retornaFalse() {
        Cliente c = clienteValido("Test", "512345678", "a@b.com");
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: teléfono empieza por 0 → false")
    void registrar_telefonoCon0_retornaFalse() {
        Cliente c = clienteValido("Test", "012345678", "a@b.com");
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: teléfono con 8 dígitos → false")
    void registrar_telefonoOchoDigitos_retornaFalse() {
        Cliente c = clienteValido("Test", "61234567", "a@b.com");
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: teléfono con 10 dígitos → false")
    void registrar_telefonoDiezDigitos_retornaFalse() {
        Cliente c = clienteValido("Test", "6123456789", "a@b.com");
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: teléfono null → false")
    void registrar_telefonoNulo_retornaFalse() {
        Cliente c = clienteValido("Test", null, "a@b.com");
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: teléfono con letras → false")
    void registrar_telefonoConLetras_retornaFalse() {
        Cliente c = clienteValido("Test", "6abc45678", "a@b.com");
        assertFalse(service.registrar(c));
    }

    // --- Validaciones email ---

    @Test
    @DisplayName("registrar: email válido estándar → true")
    void registrar_emailEstandar_retornaTrue() {
        Cliente c = clienteValido("Test", "612345678", "usuario@dominio.com");
        assertTrue(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: email con subdominio → true")
    void registrar_emailConSubdominio_retornaTrue() {
        Cliente c = clienteValido("Test", "612345678", "user@mail.empresa.es");
        assertTrue(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: email sin @ → false")
    void registrar_emailSinArroba_retornaFalse() {
        Cliente c = clienteValido("Test", "612345678", "usuariodominio.com");
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: email sin dominio → false")
    void registrar_emailSinDominio_retornaFalse() {
        Cliente c = clienteValido("Test", "612345678", "usuario@");
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: email null → false")
    void registrar_emailNulo_retornaFalse() {
        Cliente c = clienteValido("Test", "612345678", null);
        assertFalse(service.registrar(c));
    }

    @Test
    @DisplayName("registrar: email vacío → false")
    void registrar_emailVacio_retornaFalse() {
        Cliente c = clienteValido("Test", "612345678", "");
        assertFalse(service.registrar(c));
    }

    // ─────────────────────────────────────────────────────────────
    // editar()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("editar: datos válidos → delega en repo")
    void editar_datosValidos_retornaTrue() {
        when(repoMock.actualizar(1, "Nou Nom", "699999999", "nou@email.com", "notes")).thenReturn(true);
        assertTrue(service.editar(1, "Nou Nom", "699999999", "nou@email.com", "notes"));
    }

    @Test
    @DisplayName("editar: notas null se convierte en cadena vacía")
    void editar_notasNulas_seConviertenEnVacio() {
        when(repoMock.actualizar(1, "Test", "699999999", "a@b.com", "")).thenReturn(true);
        service.editar(1, "Test", "699999999", "a@b.com", null);
        verify(repoMock).actualizar(1, "Test", "699999999", "a@b.com", "");
    }

    @Test
    @DisplayName("editar: teléfono inválido → false sin llamar repo")
    void editar_telefonoInvalido_retornaFalse() {
        assertFalse(service.editar(1, "Nom", "123", "a@b.com", ""));
        verify(repoMock, never()).actualizar(anyInt(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("editar: email inválido → false sin llamar repo")
    void editar_emailInvalido_retornaFalse() {
        assertFalse(service.editar(1, "Nom", "612345678", "noesemail", ""));
        verify(repoMock, never()).actualizar(anyInt(), any(), any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────
    // eliminar()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: id existente → true")
    void eliminar_idExistente_retornaTrue() {
        when(repoMock.eliminar(3)).thenReturn(true);
        assertTrue(service.eliminar(3));
    }

    @Test
    @DisplayName("eliminar: id inexistente → false")
    void eliminar_idInexistente_retornaFalse() {
        when(repoMock.eliminar(999)).thenReturn(false);
        assertFalse(service.eliminar(999));
    }

    // ─────────────────────────────────────────────────────────────
    // listarTodos()
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarTodos: delega correctamente en el repo")
    void listarTodos_delegaRepo() {
        List<Cliente> lista = List.of(clienteValido("Ana", "612345678", "a@b.com"));
        when(repoMock.findAll()).thenReturn(lista);
        assertEquals(1, service.listarTodos().size());
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private Cliente clienteValido(String nombre, String telefono, String email) {
        Cliente c = new Cliente();
        c.setNombre(nombre);
        c.setTelefono(telefono);
        c.setEmail(email);
        c.setAdminId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        return c;
    }
}