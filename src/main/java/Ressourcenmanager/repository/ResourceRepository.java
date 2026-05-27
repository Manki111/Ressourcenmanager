package Ressourcenmanager.repository;

import Ressourcenmanager.resource.Resource;
import Ressourcenmanager.resource.ResourceType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;

import static jakarta.persistence.Persistence.createEntityManagerFactory;

@ApplicationScoped
public class ResourceRepository {

    private final EntityManagerFactory emf = createEntityManagerFactory("RessourcenmanagerPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public List<Resource> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Resource r WHERE r.active = true ORDER BY r.type, r.name",
                    Resource.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Resource> findAllIncludingInactive() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Resource r ORDER BY r.type, r.name", Resource.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Resource> findByType(ResourceType type) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Resource r WHERE r.type = :type AND r.active = true ORDER BY r.name",
                    Resource.class)
                    .setParameter("type", type)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Resource findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Resource.class, id);
        } finally {
            em.close();
        }
    }

    public void save(Resource resource) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (resource.getId() == null) {
                em.persist(resource);
            } else {
                em.merge(resource);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Resource resource = em.find(Resource.class, id);
            if (resource != null) {
                // Soft-Delete: deaktivieren statt physisch löschen (referentielle Integrität)
                resource.setActive(false);
                em.merge(resource);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public void activate(Long id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Resource resource = em.find(Resource.class, id);
            if (resource != null) {
                resource.setActive(true);
                em.merge(resource);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public void hardDelete(Long id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Resource resource = em.find(Resource.class, id);
            if (resource != null) {
                // Zuerst alle Buchungen dieser Ressource löschen (Fremdschlüssel-Constraint)
                em.createQuery("DELETE FROM Booking b WHERE b.resource.id = :id")
                        .setParameter("id", id)
                        .executeUpdate();
                // Dann die Ressource selbst entfernen
                em.remove(resource);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
