package Ressourcenmanager.booking;

import Ressourcenmanager.auth.SessionUser;
import Ressourcenmanager.repository.BookingRepository;
import Ressourcenmanager.user.UserRole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@ApplicationScoped
public class BookingService {

    @Inject
    private BookingRepository bookingRepository;

    public BookingResult createBooking(Long resourceId, SessionUser currentUser,
                                       LocalDateTime startTime, LocalDateTime endTime,
                                       String purpose) {
        if (currentUser == null) {
            return BookingResult.NOT_LOGGED_IN;
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            return BookingResult.INVALID_TIME;
        }
        return bookingRepository.save(resourceId, currentUser.getId(), startTime, endTime, purpose);
    }

    public boolean hasConflict(Long resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        return bookingRepository.hasConflict(resourceId, startTime, endTime);
    }

    public List<Booking> getMyBookings(SessionUser currentUser) {
        if (currentUser == null) return List.of();
        return bookingRepository.findByUserId(currentUser.getId());
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public boolean cancelBooking(Long bookingId, SessionUser currentUser) {
        if (currentUser == null) return false;
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        return bookingRepository.cancel(bookingId, currentUser.getId(), isAdmin);
    }

    public long getOccupancyForDay(LocalDate date) {
        LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
        return bookingRepository.countActiveBookingsForDay(dayStart, dayEnd);
    }
}
