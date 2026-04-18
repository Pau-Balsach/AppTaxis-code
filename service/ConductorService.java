package service;

import java.util.List;
import model.Conductor;
import repository.ConductorRepository;

public class ConductorService {

    private static final String REGEX_MATRICULA = "^[0-9]{4}[A-Z]{3}$";

    private final ConductorRepository repo = new ConductorRepository();

    public boolean registrar(Conductor conductor) {
        System.out.println("[ConductorService] registrar() — matricula: " + conductor.getMatricula());
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            System.err.println("[ConductorService] registrar() BLOQUEADO por checkAuth(): " + e.getMessage());
            return false;
        }
        if (conductor.getMatricula() == null || !conductor.getMatricula().matches(REGEX_MATRICULA)) {
            System.err.println("[ConductorService] Matrícula inválida: " + conductor.getMatricula());
            return false;
        }
        if (conductor.getNombre() == null || conductor.getNombre().isBlank()) {
            System.err.println("[ConductorService] Nombre vacío.");
            return false;
        }
        if (repo.existeMatricula(conductor.getMatricula())) {
            System.err.println("[ConductorService] Matrícula ya existente: " + conductor.getMatricula());
            return false;
        }
        repo.guardar(conductor);
        System.out.println("[ConductorService] Conductor guardado correctamente.");
        return true;
    }

    public List<Conductor> listarTodos() {
        System.out.println("[ConductorService] listarTodos()");
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            System.err.println("[ConductorService] listarTodos() BLOQUEADO: " + e.getMessage());
            throw e; 
        }
        List<Conductor> lista = repo.findAll();
        System.out.println("[ConductorService] conductores encontrados: " + lista.size());
        return lista;
    }

    public boolean editar(int id, String nuevoNombre) {
        System.out.println("[ConductorService] editar() — id: " + id + " nuevoNombre: " + nuevoNombre);
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            System.err.println("[ConductorService] editar() BLOQUEADO: " + e.getMessage());
            return false;
        }
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            System.err.println("[ConductorService] Nombre vacío en editar().");
            return false;
        }
        boolean ok = repo.actualizarNombre(id, nuevoNombre.trim());
        System.out.println("[ConductorService] editar() resultado: " + ok);
        return ok;
    }

    public boolean eliminar(int id) {
        System.out.println("[ConductorService] eliminar() — id: " + id);
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            System.err.println("[ConductorService] eliminar() BLOQUEADO: " + e.getMessage());
            return false;
        }
        boolean ok = repo.eliminar(id);
        System.out.println("[ConductorService] eliminar() resultado: " + ok);
        return ok;
    }
}