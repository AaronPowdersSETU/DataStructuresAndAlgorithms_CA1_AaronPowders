package com.campsite.manager;

import com.campsite.datastructures.MyLinkedList;
import com.campsite.models.Booking;
import com.campsite.models.CampingArea;
import com.campsite.models.Campsite;
import com.campsite.models.Guest;
import com.campsite.models.Plot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


class CampsiteSystemTest {

    private CampsiteSystem system;
    private Campsite campsite;
    private CampingArea lakeArea;
    private CampingArea forestArea;
    private Plot bungalow;
    private Plot pitch;
    private Guest guest;

    // Dates in the future, so the bookings count as pending.
    private LocalDate start = LocalDate.now().plusDays(10);
    private LocalDate end = LocalDate.now().plusDays(13);   // 3 nights

    // Builds a campsite before every test.
    @BeforeEach
    void setUp() {
        system = new CampsiteSystem();

        campsite = new Campsite("Sunny Shores", "Wexford", 4, "053 1234567",
                MyLinkedList.fromCommaText("WiFi, Pool"));
        system.addCampsite(campsite);

        lakeArea = new CampingArea("Lake View Area 1", "By the lake");
        forestArea = new CampingArea("Forest Area", "In the trees");
        system.addCampingArea(campsite, lakeArea);
        system.addCampingArea(campsite, forestArea);

        bungalow = new Plot("B1", Plot.BUNGALOW, false, 2, 4, 50,
                MyLinkedList.fromCommaText("Air Conditioning"), 100, "http://photo/b1");
        pitch = new Plot("P1", Plot.EMPTY_PITCH, true, 0, 0, 30,
                new MyLinkedList<>(), 25, "http://photo/p1");
        system.addPlot(lakeArea, bungalow);
        system.addPlot(lakeArea, pitch);

        guest = system.addGuest("Mary Murphy", LocalDate.of(1990, 5, 1),
                "Cork", "087 1111111", "mary@email.com", "");
    }

    @Test
    void duplicateAreaIdentifierIsRejected() {
        CampingArea copy = new CampingArea("lake view area 1", "Same name, different case");
        assertFalse(system.addCampingArea(campsite, copy));
        assertEquals(2, campsite.getAreas().size());
    }

    @Test
    void staticPlotAlwaysHasPower() {
        // We passed "false" for power above, but bungalows must have power.
        assertTrue(bungalow.hasPower());
    }

    @Test
    void bookingIsAssignedToMatchingPlot() {
        Booking b = system.makeBooking(campsite, lakeArea, guest, start, end,
                Plot.BUNGALOW, 3, true, 0, 2, 500);

        assertNotNull(b);
        assertEquals(bungalow, b.getPlot());
        assertEquals(300, b.getTotalCost());           // 3 nights x €100.
        assertEquals(1, guest.getBookings().size());
    }

    @Test
    void bookingFailsWhenOverBudget() {
        // 3 nights x €100 = €300, but the budget is only €200.
        Booking b = system.makeBooking(campsite, lakeArea, guest, start, end,
                Plot.BUNGALOW, 2, false, 0, 0, 200);
        assertNull(b);
    }

    @Test
    void plotCannotBeDoubleBooked() {
        Booking first = system.makeBooking(campsite, lakeArea, guest, start, end,
                Plot.BUNGALOW, 2, false, 0, 0, 500);
        // Overlapping dates for the same (only) bungalow.
        Booking second = system.makeBooking(campsite, lakeArea, guest, start.plusDays(1), end.plusDays(1),
                Plot.BUNGALOW, 2, false, 0, 0, 500);

        assertNotNull(first);
        assertNull(second);
    }

    @Test
    void cancelRemovesBookingFromPlotAndGuest() {
        Booking b = system.makeBooking(campsite, lakeArea, guest, start, end,
                Plot.EMPTY_PITCH, 2, true, 0, 0, 500);
        system.cancelBooking(b);

        assertTrue(pitch.getBookings().isEmpty());
        assertTrue(guest.getBookings().isEmpty());
    }

    @Test
    void removePlotMovesBookingToAnotherArea() {
        // A second pitch in a different area of the same campsite
        Plot forestPitch = new Plot("F1", Plot.EMPTY_PITCH, true, 0, 0, 40,
                new MyLinkedList<>(), 20, "");
        system.addPlot(forestArea, forestPitch);

        Booking b = system.makeBooking(campsite, lakeArea, guest, start, end,
                Plot.EMPTY_PITCH, 2, true, 0, 0, 500);
        assertEquals(pitch, b.getPlot());

        MyLinkedList<Booking> notMoved = system.removePlot(campsite, lakeArea, pitch);

        assertTrue(notMoved.isEmpty());
        assertEquals(forestPitch, b.getPlot());
        assertNull(lakeArea.findPlot("P1"));
    }

    @Test
    void removePlotCancelsBookingIfNowhereToMove() {
        Booking b = system.makeBooking(campsite, lakeArea, guest, start, end,
                Plot.EMPTY_PITCH, 2, true, 0, 0, 500);

        MyLinkedList<Booking> notMoved = system.removePlot(campsite, lakeArea, pitch);

        assertEquals(1, notMoved.size());
        assertFalse(guest.getBookings().contains(b));
    }

    @Test
    void smartFindPicksCheapestAndRespectsFacilities() {
        // Wants a pool (campsite facility) and air conditioning (only the bungalow has it)
        Booking result = system.smartFind(guest, start, 3, 2, 1000, 3, "Any", false,
                MyLinkedList.fromCommaText("Pool, Air Conditioning"));

        assertNotNull(result);
        assertEquals(bungalow, result.getPlot());
    }

    @Test
    void smartFindMovesToLaterDateWhenFull() {
        // Fill the only pitch on the desired dates.
        system.makeBooking(campsite, lakeArea, guest, start, end, Plot.EMPTY_PITCH, 2, true, 0, 0, 500);

        Booking result = system.smartFind(guest, start, 3, 2, 500, 0, Plot.EMPTY_PITCH, true,
                new MyLinkedList<>());

        assertNotNull(result);
        assertEquals(end, result.getStartDate());   // first free day is the old check-out day.
    }

    @Test
    void resetClearsEverything() {
        system.reset();
        assertTrue(system.getCampsites().isEmpty());
        assertTrue(system.getGuests().isEmpty());
    }

    @Test
    void saveAndLoadKeepsAllData() throws Exception {
        system.makeBooking(campsite, lakeArea, guest, start, end, Plot.BUNGALOW, 2, false, 0, 0, 500);
        String fileName = "test-save.dat";
        system.save(fileName);

        CampsiteSystem loaded = new CampsiteSystem();
        loaded.load(fileName);

        assertEquals(1, loaded.getCampsites().size());
        assertEquals(1, loaded.findGuest(1).getBookings().size());
        assertNotNull(loaded.findCampsite("Sunny Shores").findArea("Lake View Area 1").findPlot("B1"));

        new File(fileName).delete();   // tidy up the test file.
    }
}
