package service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import model.Viaje;
import repository.ViajeRepository;

public class ViajeService {

    private final ViajeRepository repo = new ViajeRepository();

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

    public List<Viaje> listarPorConductor(int conductorId) {
        return repo.findByConductor(conductorId);
    }

    public List<Viaje> listarPorFecha(int conductorId, LocalDate fecha) {
        return repo.findByConductorAndFecha(conductorId, fecha);
    }

    public List<Viaje> listarPorMes(int anio, int mes) {
        return repo.findByMes(anio, mes);
    }
    
    public boolean editar(Viaje viaje) {
        return repo.actualizar(viaje);
    }

    public boolean eliminar(UUID id) {
        return repo.eliminar(id);
    }
}