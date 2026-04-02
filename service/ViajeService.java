package service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import model.Viaje;
import repository.ViajeRepository;

/**
 * Lógica de negocio para Viaje.
 * No conoce JPA ni EntityManager; delega toda la persistencia en ViajeRepository.
 */
public class ViajeService {

    private final ViajeRepository repo = new ViajeRepository();

    /**
     * Crea un nuevo viaje asignándolo al conductor indicado.
     * @return true si se guardó correctamente, false si el conductor no existe.
     */
    public boolean crear(Viaje viaje, int conductorId) {
        try {
            repo.guardar(viaje, conductorId);
            return true;
        } catch (IllegalArgumentException e) {
            // Conductor no encontrado
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Devuelve todos los viajes de un conductor, ordenados por fecha y hora. */
    public List<Viaje> listarPorConductor(int conductorId) {
        return repo.findByConductor(conductorId);
    }

    /** Devuelve los viajes de un conductor en una fecha concreta, ordenados por hora. */
    public List<Viaje> listarPorFecha(int conductorId, LocalDate fecha) {
        return repo.findByConductorAndFecha(conductorId, fecha);
    }

    /**
     * Edita los campos de un viaje existente.
     * @return true si se actualizó, false si el viaje no existe.
     */
    public boolean editar(Viaje viaje) {
        return repo.actualizar(viaje);
    }

    /**
     * Elimina un viaje por su UUID.
     * @return true si se eliminó, false si no existía.
     */
    public boolean eliminar(UUID id) {
        return repo.eliminar(id);
    }
}