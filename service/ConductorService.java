package service;

import java.util.List;
import java.util.UUID;
import model.Admin;
import model.Conductor;
import repository.ConductorRepository;

public class ConductorService {

    private static final String REGEX_MATRICULA = "^[0-9]{4}[A-Z]{3}$";
    private final ConductorRepository repo = new ConductorRepository();

    public boolean registrar(Conductor conductor) {
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            return false;
        }
        if (conductor.getMatricula() == null || !conductor.getMatricula().matches(REGEX_MATRICULA)) return false;
        if (conductor.getNombre() == null || conductor.getNombre().isBlank()) return false;
        if (repo.existeMatricula(conductor.getMatricula())) return false;
        repo.guardar(conductor);
        return true;
    }

    public List<Conductor> listarTodos() {
        SessionManager.checkAuth();
        Admin admin = SessionManager.getAdmin();
        if (admin == null || admin.getId() == null) {
            throw new SecurityException("Acceso Denegado: No hay administrador en sesion.");
        }
        return repo.findAllByAdminId(admin.getId());
    }

    public boolean editar(int id, String nuevoNombre) {
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            return false;
        }
        if (nuevoNombre == null || nuevoNombre.isBlank()) return false;
        return repo.actualizarNombre(id, nuevoNombre.trim());
    }

    public boolean eliminar(int id) {
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            return false;
        }
        return repo.eliminar(id);
    }
}