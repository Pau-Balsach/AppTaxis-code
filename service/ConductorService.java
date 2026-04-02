package service;

import java.util.List;
import model.Conductor;
import repository.ConductorRepository;

/**
 * Lógica de negocio para Conductor.
 * No conoce JPA ni EntityManager; delega toda la persistencia en ConductorRepository.
 */
public class ConductorService {

    private static final String REGEX_MATRICULA = "^[0-9]{4}[A-Z]{3}$";

    private final ConductorRepository repo = new ConductorRepository();

    /**
     * Registra un nuevo conductor validando formato de matrícula y unicidad.
     * @return true si se guardó, false si la matrícula ya existe o es inválida.
     */
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

    /** Devuelve todos los conductores. */
    public List<Conductor> listarTodos() {
        return repo.findAll();
    }

    /**
     * Cambia el nombre de un conductor.
     * @return true si se actualizó, false si el id no existe o el nombre está vacío.
     */
    public boolean editar(int id, String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) return false;
        return repo.actualizarNombre(id, nuevoNombre.trim());
    }

    /**
     * Elimina un conductor por id.
     * @return true si se eliminó, false si no existía.
     */
    public boolean eliminar(int id) {
        return repo.eliminar(id);
    }
}