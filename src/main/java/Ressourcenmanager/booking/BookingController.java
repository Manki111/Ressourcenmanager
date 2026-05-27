package Ressourcenmanager.booking;

import Ressourcenmanager.auth.AuthController;
import Ressourcenmanager.dto.BookingDTO;
import Ressourcenmanager.resource.Resource;
import Ressourcenmanager.resource.ResourceService;
import Ressourcenmanager.resource.ResourceType;
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
public class BookingController implements Serializable {

    @Inject
    private BookingService bookingService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private AuthController authController;

    private List<Booking> myBookings;
    private List<Resource> availableResources;

    private BookingDTO newBooking = new BookingDTO();

    private boolean conflictChecked = false;
    private boolean conflictFound = false;

    @PostConstruct
    public void init() {
        loadMyBookings();
        availableResources = resourceService.getAllActive();
    }

    private void loadMyBookings() {
        myBookings = bookingService.getMyBookings(authController.getCurrentUser());
    }

    public void checkAvailability() {
        conflictChecked = false;
        conflictFound = false;

        if (newBooking.getResourceId() == null
                || newBooking.getStartTime() == null
                || newBooking.getEndTime() == null) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Eingabe unvollständig",
                    "Bitte Ressource, Start- und Endzeit auswählen.");
            return;
        }

        if (!newBooking.getEndTime().isAfter(newBooking.getStartTime())) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Ungültiger Zeitraum",
                    "Die Endzeit muss nach der Startzeit liegen.");
            return;
        }

        conflictFound = bookingService.hasConflict(
                newBooking.getResourceId(),
                newBooking.getStartTime(),
                newBooking.getEndTime()
        );
        conflictChecked = true;

        if (conflictFound) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Zeitraum belegt",
                    "Die Ressource ist im gewählten Zeitraum bereits gebucht.");
        } else {
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Verfügbar",
                    "Die Ressource ist im gewählten Zeitraum verfügbar.");
        }
    }

    public void saveBooking() {
        if (authController.getCurrentUser() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nicht angemeldet",
                    "Bitte melden Sie sich an, um eine Buchung vorzunehmen.");
            return;
        }

        if (newBooking.getResourceId() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Ressource fehlt",
                    "Bitte wählen Sie eine Ressource aus.");
            return;
        }

        if (newBooking.getStartTime() == null || newBooking.getEndTime() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Zeitraum fehlt",
                    "Bitte geben Sie Start- und Endzeit an.");
            return;
        }

        if (!newBooking.getEndTime().isAfter(newBooking.getStartTime())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Ungültiger Zeitraum",
                    "Die Endzeit muss nach der Startzeit liegen.");
            return;
        }

        BookingResult result = bookingService.createBooking(
                newBooking.getResourceId(),
                authController.getCurrentUser(),
                newBooking.getStartTime(),
                newBooking.getEndTime(),
                newBooking.getPurpose()
        );

        switch (result) {
            case SUCCESS -> {
                addMessage(FacesMessage.SEVERITY_INFO, "Buchung erfolgreich",
                        "Ihre Buchung wurde gespeichert.");
                newBooking = new BookingDTO();
                conflictChecked = false;
                conflictFound = false;
                loadMyBookings();
            }
            case CONFLICT -> addMessage(FacesMessage.SEVERITY_ERROR, "Zeitkonflikt",
                    "Die Ressource ist im gewählten Zeitraum bereits gebucht.");
            case INVALID_TIME -> addMessage(FacesMessage.SEVERITY_ERROR, "Ungültige Zeit",
                    "Bitte prüfen Sie Start- und Endzeit.");
            case RESOURCE_NOT_FOUND -> addMessage(FacesMessage.SEVERITY_ERROR, "Ressource nicht gefunden",
                    "Die gewählte Ressource existiert nicht.");
            default -> addMessage(FacesMessage.SEVERITY_ERROR, "Fehler",
                    "Die Buchung konnte nicht gespeichert werden.");
        }
    }

    public void cancelBooking(Long bookingId) {
        boolean success = bookingService.cancelBooking(bookingId, authController.getCurrentUser());
        if (success) {
            addMessage(FacesMessage.SEVERITY_INFO, "Storniert",
                    "Die Buchung wurde erfolgreich storniert.");
            loadMyBookings();
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Stornierung fehlgeschlagen",
                    "Die Buchung konnte nicht storniert werden.");
        }
    }

    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public List<Booking> getMyBookings() { return myBookings; }
    public List<Resource> getAvailableResources() { return availableResources; }
    public BookingDTO getNewBooking() { return newBooking; }
    public void setNewBooking(BookingDTO newBooking) { this.newBooking = newBooking; }
    public boolean isConflictChecked() { return conflictChecked; }
    public boolean isConflictFound() { return conflictFound; }
    public boolean hasMyBookings() { return myBookings != null && !myBookings.isEmpty(); }
}
