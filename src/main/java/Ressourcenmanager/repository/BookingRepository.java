package Ressourcenmanager.repository;

import Ressourcenmanager.booking.Booking;
import Ressourcenmanager.booking.BookingResult;
import Ressourcenmanager.booking.BookingStatus;
import Ressourcenmanager.resource.Resource;
import Ressourcenmanager.user.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.Persistence.createEntityManagerFactory;

@ApplicationScoped
public class BookingRepository {

    private final EntityManagerFactory emf = createEntityManagerFactory("RessourcenmanagerPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public boolean hasConflict(Long resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        EntityManager em = getEntityManager();
        try {
            String jpql = """
                    SELECT COUNT(b) FROM Booking b
                    WHERE b.resource.id = :resourceId
                    AND b.status = :status
                    AND b.startTime < :endTime
                    AND b.endTime > :startTime
                    """;

            var query = em.createQuery(jpql, Long.class)
                    .setParameter("resourceId", resourceId)
                    .setParameter("status", BookingStatus.AKTIV)
                    .setParameter("startTime", startTime)
                    .setParameter("endTime", endTime);


            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public BookingResult save(Long resourceId, Long userId,
                               LocalDateTime startTime, LocalDateTime endTime, String purpose) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Resource resource = em.find(Resource.class, resourceId);
            if (resource == null) {
                tx.rollback();
                return BookingResult.RESOURCE_NOT_FOUND;
            }

            User user = em.find(User.class, userId);
            if (user == null) {
                tx.rollback();
                return BookingResult.USER_NOT_FOUND;
            }

            Long conflicts = em.createQuery(
                    """
                    SELECT COUNT(b) FROM Booking b
                    WHERE b.resource.id = :resourceId
                    AND b.status = :status
                    AND b.startTime < :endTime
                    AND b.endTime > :startTime
                    """, Long.class)
                    .setParameter("resourceId", resourceId)
                    .setParameter("status", BookingStatus.AKTIV)
                    .setParameter("startTime", startTime)
                    .setParameter("endTime", endTime)
                    .getSingleResult();

            if (conflicts > 0) {
                tx.rollback();
                return BookingResult.CONFLICT;
            }

            Booking booking = new Booking(resource, user, startTime, endTime, purpose);
            em.persist(booking);
            tx.commit();
            return BookingResult.SUCCESS;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public List<Booking> findByUserId(Long userId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    """
                    SELECT b FROM Booking b
                    JOIN FETCH b.resource
                    WHERE b.user.id = :userId
                    ORDER BY b.startTime DESC
                    """, Booking.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Booking> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    """
                    SELECT b FROM Booking b
                    JOIN FETCH b.resource
                    JOIN FETCH b.user
                    ORDER BY b.startTime DESC
                    """, Booking.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean cancel(Long bookingId, Long requestingUserId, boolean isAdmin) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Booking booking = em.find(Booking.class, bookingId);
            if (booking == null || booking.getStatus() == BookingStatus.STORNIERT) {
                tx.rollback();
                return false;
            }

            if (!isAdmin && !booking.getUser().getId().equals(requestingUserId)) {
                tx.rollback();
                return false;
            }
            booking.setStatus(BookingStatus.STORNIERT);
            em.merge(booking);
            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }


    public long countActiveBookingsForDay(LocalDateTime dayStart, LocalDateTime dayEnd) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    """
                    SELECT COUNT(DISTINCT b.user.id) FROM Booking b
                    WHERE b.status = :status
                    AND b.startTime < :dayEnd
                    AND b.endTime > :dayStart
                    """, Long.class)
                    .setParameter("status", BookingStatus.AKTIV)
                    .setParameter("dayStart", dayStart)
                    .setParameter("dayEnd", dayEnd)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}
