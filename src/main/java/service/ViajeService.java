package service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import model.Admin;
import model.Viaje;
import repository.ViajeRepository;

public class ViajeService {

    private final ViajeRepository repo = new ViajeRepository();

    public boolean crear(Viaje viaje, int conductorId) {
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            return false;
        }
        try {
            repo.guardar(viaje, conductorId);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Viaje> listarPorConductor(int conductorId) {
        SessionManager.checkAuth();
        return repo.findByConductor(conductorId, getAdminId());
    }

    public List<Viaje> listarTodos() {
        SessionManager.checkAuth();
        return repo.findAll(getAdminId());
    }

    public List<Viaje> listarPorFecha(int conductorId, LocalDate fecha) {
        SessionManager.checkAuth();
        return repo.findByConductorAndFecha(conductorId, fecha, getAdminId());
    }

    public List<Viaje> listarPorMes(int anio, int mes) {
        SessionManager.checkAuth();
        return repo.findByMes(anio, mes, getAdminId());
    }

    public boolean editar(Viaje viaje) {
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            return false;
        }
        return repo.actualizar(viaje);
    }

    public boolean eliminar(UUID id) {
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            return false;
        }
        return repo.eliminar(id);
    }

    private UUID getAdminId() {
        Admin admin = SessionManager.getAdmin();
        if (admin == null || admin.getId() == null) {
            throw new SecurityException("Acceso Denegado: No hay administrador en sesion.");
        }
        return admin.getId();
    }
}