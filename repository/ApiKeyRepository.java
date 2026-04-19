package repository;

import jakarta.persistence.EntityManager;

public class ApiKeyRepository {
    public boolean esValida(String hashEnviado) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(a) FROM ApiKey a WHERE a.keyHash = :h AND a.activa = true", Long.class)
                .setParameter("h", hashEnviado)
                .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}