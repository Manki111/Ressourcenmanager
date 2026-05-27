package Ressourcenmanager.booking;

import Ressourcenmanager.auth.AuthController;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class AdminBookingController implements Serializable {

    @Inject
    private BookingService bookingService;

    @Inject
    private AuthController authController;

    private List<Booking> allBookings;

    @PostConstruct
    public void init() {
        loadAllBookings();
    }

    private void loadAllBookings() {
        allBookings = bookingService.getAllBookings();
    }

    public void cancelBooking(Long bookingId) {
        boolean success = bookingService.cancelBooking(bookingId, authController.getCurrentUser());
        if (success) {
            addMessage(FacesMessage.SEVERITY_INFO, "Storniert",
                    "Die Buchung wurde erfolgreich storniert.");
            loadAllBookings();
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Fehler",
                    "Die Buchung konnte nicht storniert werden.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public List<Booking> getAllBookings() { return allBookings; }
    public boolean hasBookings() { return allBookings != null && !allBookings.isEmpty(); }
}
