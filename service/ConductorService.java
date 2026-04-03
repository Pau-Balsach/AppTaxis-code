package service;

import java.util.List;
import model.Conductor;
import repository.ConductorRepository;

public class ConductorService {

    private static final String REGEX_MATRICULA = "^[0-9]{4}[A-Z]{3}$";

    private final ConductorRepository repo = new ConductorRepository();

    public boolean registrar(Conductor conductor) {
        if (conductor.getMatricula() == null || !conductor.getMatricula().matches(REGEX_MATRICULA))
            return false;
        if (conductor.getNombre() == null || conductor.getNombre().isBlank())
            return false;
        if (repo.existeMatricula(conductor.getMatricula()))
            return false;

        repo.guardar(conductor);
        return true;
    }

    public List<Conductor> listarTodos() {
        return repo.findAll();
    }
    
    public boolean editar(int id, String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) return false;
        return repo.actualizarNombre(id, nuevoNombre.trim());
    }

    public boolean eliminar(int id) {
        return repo.eliminar(id);
    }
}