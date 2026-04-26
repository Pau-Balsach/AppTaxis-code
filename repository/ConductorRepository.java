package repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import model.Conductor;

public class ConductorRepository {

    public List<Conductor> findAllByAdminId(UUID adminId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                        "SELECT c FROM Conductor c WHERE c.cond_admin = :adminId",
                        Conductor.class)
                     .setParameter("adminId", adminId)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public Conductor findById(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Conductor.class, id);
        } finally {
            em.close();
        }
    }

    public boolean existeMatricula(String matricula) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(c) FROM Conductor c WHERE c.matricula = :mat", Long.class)
                .setParameter("mat", matricula)
                .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public void guardar(Conductor conductor) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(conductor);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean actualizarNombre(int id, String nuevoNombre) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Conductor c = em.find(Conductor.class, id);
            if (c == null) return false;
            c.setNombre(nuevoNombre);
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

    public boolean eliminar(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Conductor c = em.find(Conductor.class, id);
            if (c == null) return false;
            em.remove(c);
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
}
