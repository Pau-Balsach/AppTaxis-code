package repository;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import model.Conductor;
import model.Viaje;

public class ViajeRepository {

    public void guardar(Viaje viaje, int conductorId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Conductor c = em.find(Conductor.class, conductorId);
            if (c == null) throw new IllegalArgumentException("Conductor no encontrado: " + conductorId);
            viaje.setConductor(c);
            em.persist(viaje);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Viaje> findByConductor(int conductorId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT v FROM Viaje v WHERE v.conductor.id = :id ORDER BY v.dia, v.hora",
                Viaje.class)
                .setParameter("id", conductorId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Viaje> findByConductorAndFecha(int conductorId, LocalDate fecha) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT v FROM Viaje v WHERE v.conductor.id = :id AND v.dia = :fecha ORDER BY v.hora",
                Viaje.class)
                .setParameter("id", conductorId)
                .setParameter("fecha", fecha)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Viaje> findByMes(int anio, int mes) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            LocalDate inicio = LocalDate.of(anio, mes, 1);
            LocalDate fin    = inicio.withDayOfMonth(inicio.lengthOfMonth());
            return em.createQuery(
                "SELECT v FROM Viaje v JOIN FETCH v.conductor WHERE v.dia BETWEEN :ini AND :fin ORDER BY v.dia, v.hora",
                Viaje.class)
                .setParameter("ini", inicio)
                .setParameter("fin", fin)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean actualizar(Viaje viaje) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Viaje v = em.find(Viaje.class, viaje.getId());
            if (v == null) return false;
            v.setDia(viaje.getDia());
            v.setHora(viaje.getHora());
            v.setHoraFinalizacion(viaje.getHoraFinalizacion());
            v.setPuntorecogida(viaje.getPuntorecogida());
            v.setPuntodejada(viaje.getPuntodejada());
            v.setTelefonocliente(viaje.getTelefonocliente());
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean eliminar(UUID id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Viaje v = em.find(Viaje.class, id);
            if (v == null) return false;
            em.remove(v);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public List<Viaje> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT v FROM Viaje v", Viaje.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }
}
