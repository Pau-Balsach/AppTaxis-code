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
        System.out.println("[ViajeService] crear() — conductorId: " + conductorId
                + " dia: " + viaje.getDia() + " hora: " + viaje.getHora());
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            System.err.println("[ViajeService] crear() BLOQUEADO: " + e.getMessage());
            return false;
        }
        try {
            repo.guardar(viaje, conductorId);
            System.out.println("[ViajeService] Viaje guardado correctamente.");
            return true;
        } catch (IllegalArgumentException e) {
            System.err.println("[ViajeService] crear() — conductor no encontrado: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[ViajeService] crear() — excepción inesperada: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Viaje> listarPorConductor(int conductorId) {
        System.out.println("[ViajeService] listarPorConductor() — conductorId: " + conductorId);
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            System.err.println("[ViajeService] listarPorConductor() BLOQUEADO: " + e.getMessage());
            throw e;
        }
        UUID adminId = getAdminId();
        List<Viaje> lista = repo.findByConductor(conductorId, adminId);
        System.out.println("[ViajeService] viajes encontrados: " + lista.size());
        return lista;
    }

    public List<Viaje> listarTodos() {
        System.out.println("[ViajeService] listarTodos()");
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            System.err.println("[ViajeService] listarTodos() BLOQUEADO: " + e.getMessage());
            throw e;
        }
        UUID adminId = getAdminId();
        List<Viaje> lista = repo.findAll(adminId);
        System.out.println("[ViajeService] total viajes: " + lista.size());
        return lista;
    }

    public List<Viaje> listarPorFecha(int conductorId, LocalDate fecha) {
        System.out.println("[ViajeService] listarPorFecha() — conductorId: " + conductorId + " fecha: " + fecha);
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            System.err.println("[ViajeService] listarPorFecha() BLOQUEADO: " + e.getMessage());
            throw e;
        }
        UUID adminId = getAdminId();
        List<Viaje> lista = repo.findByConductorAndFecha(conductorId, fecha, adminId);
        System.out.println("[ViajeService] viajes en fecha: " + lista.size());
        return lista;
    }

    public List<Viaje> listarPorMes(int anio, int mes) {
        System.out.println("[ViajeService] listarPorMes() — " + anio + "/" + mes);
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            System.err.println("[ViajeService] listarPorMes() BLOQUEADO: " + e.getMessage());
            throw e;
        }
        UUID adminId = getAdminId();
        List<Viaje> lista = repo.findByMes(anio, mes, adminId);
        System.out.println("[ViajeService] viajes en mes: " + lista.size());
        return lista;
    }

    public boolean editar(Viaje viaje) {
        System.out.println("[ViajeService] editar() — id: " + viaje.getId());
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            System.err.println("[ViajeService] editar() BLOQUEADO: " + e.getMessage());
            return false;
        }
        boolean ok = repo.actualizar(viaje);
        System.out.println("[ViajeService] editar() resultado: " + ok);
        return ok;
    }

    public boolean eliminar(UUID id) {
        System.out.println("[ViajeService] eliminar() — id: " + id);
        try {
            SessionManager.checkAuth();
        } catch (SecurityException e) {
            System.err.println("[ViajeService] eliminar() BLOQUEADO: " + e.getMessage());
            return false;
        }
        boolean ok = repo.eliminar(id);
        System.out.println("[ViajeService] eliminar() resultado: " + ok);
        return ok;
    }

    private UUID getAdminId() {
        Admin admin = SessionManager.getAdmin();
        if (admin == null || admin.getId() == null) {
            throw new SecurityException("Acceso Denegado: No hay administrador en sesion.");
        }
        return admin.getId();
    }
}