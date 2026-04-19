package service;

import java.util.List;
import model.Cliente;
import repository.ClienteRepository;

public class ClienteService {

    private static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    private final ClienteRepository repo = new ClienteRepository();

    public boolean registrar(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) return false;
        if (cliente.getTelefono() == null || cliente.getTelefono().isBlank()) return false;
        if (cliente.getEmail() == null || !cliente.getEmail().matches(REGEX_EMAIL)) return false;

        repo.guardar(cliente);
        return true;
    }

    public List<Cliente> listarTodos() {
        return repo.findAll();
    }

    public boolean editar(int id, String nombre, String telefono, String email, String notas) {
        if (nombre == null || nombre.isBlank()) return false;
        if (telefono == null || telefono.isBlank()) return false;
        if (email == null || !email.matches(REGEX_EMAIL)) return false;

        return repo.actualizar(id, nombre.trim(), telefono.trim(), email.trim(), notas == null ? "" : notas.trim());
    }

    public boolean eliminar(int id) {
        return repo.eliminar(id);
    }
}