package repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import model.Conductor;

/**
 * Acceso a datos para Conductor.
 * Solo habla con JPA; no contiene reglas de negocio.
 */
public class ConductorRepository {

    /** Devuelve todos los conductores. */
    public List<Conductor> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Conductor c", Conductor.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    /** Busca un conductor por su PK. Devuelve null si no existe. */
    public Conductor findById(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Conductor.class, id);
        } finally {
            em.close();
        }
    }

    /** Comprueba si ya existe un conductor con esa matrícula. */
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

    /** Persiste un nuevo conductor. */
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

    /** Actualiza el nombre de un conductor existente. Devuelve false si no se encuentra. */
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

    /** Elimina un conductor por su PK. Devuelve false si no se encuentra. */
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