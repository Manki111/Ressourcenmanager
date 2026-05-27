package Ressourcenmanager.booking;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class OccupancyController implements Serializable {

    @Inject
    private BookingService bookingService;

    private List<OccupancyDay> occupancyDays;

    @PostConstruct
    public void init() {
        loadOccupancy();
    }

    private void loadOccupancy() {
        occupancyDays = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            long count = bookingService.getOccupancyForDay(date);
            occupancyDays.add(new OccupancyDay(date, count));
        }
    }

    public List<OccupancyDay> getOccupancyDays() { return occupancyDays; }

    public static class OccupancyDay implements Serializable {
        private final LocalDate date;
        private final long bookedUsers;

        public OccupancyDay(LocalDate date, long bookedUsers) {
            this.date = date;
            this.bookedUsers = bookedUsers;
        }

        public LocalDate getDate() { return date; }
        public long getBookedUsers() { return bookedUsers; }

        public String getOccupancyLevel() {
            if (bookedUsers == 0) return "leer";
            if (bookedUsers <= 1) return "gering";
            if (bookedUsers <= 2) return "mittel";
            return "hoch";
        }
    }
}
