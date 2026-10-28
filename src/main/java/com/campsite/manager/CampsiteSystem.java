package com.campsite.manager;

import com.campsite.datastructures.MyLinkedList;
import com.campsite.models.Booking;
import com.campsite.models.CampingArea;
import com.campsite.models.Campsite;
import com.campsite.models.Guest;
import com.campsite.models.Plot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;


public class CampsiteSystem {

    private MyLinkedList<Campsite> campsites;
    private MyLinkedList<Guest> guests;
    private int nextGuestId;     // used to give each guest a unique number.
    private int nextBookingId;   // used to give each booking a unique number.

    public CampsiteSystem() {
        campsites = new MyLinkedList<>();
        guests = new MyLinkedList<>();
        nextGuestId = 1;
        nextBookingId = 1;
    }

    // ------------------------------------------------------------
    // Adding things
    // ------------------------------------------------------------

    // Adds a campsite if no campsite already has that name. Returns true if added.
    public boolean addCampsite(Campsite campsite) {
        if (findCampsite(campsite.getName()) != null) {
            return false;
        }
        campsites.add(campsite);
        return true;
    }

    // Finds a campsite by name (ignores case). Returns null if not found.
    public Campsite findCampsite(String name) {
        for (Campsite c : campsites) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    // Adds a camping area to a campsite. The Campsite class checks the identifier is unique.
    public boolean addCampingArea(Campsite campsite, CampingArea area) {
        return campsite.addArea(area);
    }

    // Adds a plot to a camping area. The CampingArea class checks the identifier is unique.
    public boolean addPlot(CampingArea area, Plot plot) {
        return area.addPlot(plot);
    }

    // Creates a guest with the next free id number and returns it.
    public Guest addGuest(String name, LocalDate dob, String address, String phone, String email, String extraInfo) {
        Guest guest = new Guest(nextGuestId, name, dob, address, phone, email, extraInfo);
        nextGuestId++;
        guests.add(guest);
        return guest;
    }

    // Finds a guest by id. Returns null if not found.
    public Guest findGuest(int id) {
        for (Guest g : guests) {
            if (g.getId() == id) {
                return g;
            }
        }
        return null;
    }

    // ------------------------------------------------------------
    // Bookings
    // ------------------------------------------------------------


    public boolean plotMatches(Plot plot, Booking request) {
        // Type (blank or "Any" means any type is fine)
        String type = request.getPreferredType();
        if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("Any")) {
            if (!plot.getType().equalsIgnoreCase(type)) {
                return false;
            }
        }
        // Power
        if (request.needsPower() && !plot.hasPower()) {
            return false;
        }
        // Size
        if (plot.getSize() < request.getMinSize()) {
            return false;
        }
        // Bedrooms
        if (plot.getBedrooms() < request.getMinBedrooms()) {
            return false;
        }
        // Enough beds (only static dwellings have beds; pitches bring their own tent/caravan)
        if (plot.isStatic() && plot.getBeds() < request.getNumberOfGuests()) {
            return false;
        }
        // Budget (total cost of the stay must be within budget)
        double cost = request.getNights() * plot.getCostPerNight();
        if (cost > request.getBudget()) {
            return false;
        }
        // Free on those dates
        return plot.isAvailable(request.getStartDate(), request.getEndDate());
    }

    // Searches one camping area and returns the first suitable plot, or null if none.
    public Plot findSuitablePlot(CampingArea area, Booking request) {
        for (Plot plot : area.getPlots()) {
            if (plotMatches(plot, request)) {
                return plot;
            }
        }
        return null;
    }


    public Booking makeBooking(Campsite campsite, CampingArea area, Guest guest,
                               LocalDate start, LocalDate end, String type, int numberOfGuests,
                               boolean needsPower, double minSize, int minBedrooms, double budget) {
        // The end date must be after the start date
        if (!end.isAfter(start)) {
            return null;
        }

        // Build the booking request with the guest's preferences.
        Booking booking = new Booking(nextBookingId, guest, start, end, type,
                numberOfGuests, needsPower, minSize, minBedrooms, budget);

        Plot plot = findSuitablePlot(area, booking);
        if (plot == null) {
            return null;   // nothing suitable, GUI will tell the user.
        }

        booking.assignTo(campsite, area, plot);
        confirmBooking(booking);
        return booking;
    }


    public void confirmBooking(Booking booking) {
        nextBookingId++;
        booking.getPlot().addBooking(booking);
        booking.getGuest().addBooking(booking);
    }

    // Cancels a booking by removing it from its plot and from its guest.
    public void cancelBooking(Booking booking) {
        if (booking.getPlot() != null) {
            booking.getPlot().removeBooking(booking);
        }
        booking.getGuest().removeBooking(booking);
    }

    // ------------------------------------------------------------
    // Remove a plot
    // ------------------------------------------------------------


    public MyLinkedList<Booking> removePlot(Campsite campsite, CampingArea area, Plot plot) {
        MyLinkedList<Booking> notMoved = new MyLinkedList<>();

        // Remove the plot first so it cannot be picked again.
        area.removePlot(plot);

        for (Booking booking : plot.getBookings()) {
            if (!booking.isPending()) {
                continue;   // past bookings stay as history on the guest's record.
            }

            // Look through every area of the same campsite for a new plot.
            boolean moved = false;
            for (CampingArea otherArea : campsite.getAreas()) {
                Plot newPlot = findSuitablePlot(otherArea, booking);
                if (newPlot != null) {
                    booking.assignTo(campsite, otherArea, newPlot);
                    newPlot.addBooking(booking);
                    moved = true;
                    break;
                }
            }

            if (!moved) {
                // No room anywhere, so the booking has to be canceled.
                booking.getGuest().removeBooking(booking);
                notMoved.add(booking);
            }
        }
        return notMoved;
    }

    // ------------------------------------------------------------
    // Smart find
    // ------------------------------------------------------------


    public Booking smartFind(Guest guest, LocalDate desiredStart, int nights, int numberOfGuests,
                             double budget, int minStars, String type, boolean needsPower,
                             MyLinkedList<String> requiredFacilities) {

        for (int dayOffset = 0; dayOffset <= 14; dayOffset++) {
            LocalDate start = desiredStart.plusDays(dayOffset);
            LocalDate end = start.plusDays(nights);

            Booking request = new Booking(nextBookingId, guest, start, end, type,
                    numberOfGuests, needsPower, 0, 0, budget);

            Booking best = null;   // cheapest match found so far for this date.

            for (Campsite campsite : campsites) {
                if (campsite.getStars() < minStars) {
                    continue;   // not enough stars, skip this campsite.
                }
                for (CampingArea area : campsite.getAreas()) {
                    for (Plot plot : area.getPlots()) {
                        if (plotMatches(plot, request)
                                && hasAllFacilities(campsite, plot, requiredFacilities)) {
                            double cost = nights * plot.getCostPerNight();
                            if (best == null || cost < best.getTotalCost()) {
                                // Make a fresh booking pointing at this cheaper plot.
                                best = new Booking(nextBookingId, guest, start, end, type,
                                        numberOfGuests, needsPower, 0, 0, budget);
                                best.assignTo(campsite, area, plot);
                            }
                        }
                    }
                }
            }

            if (best != null) {
                return best;   // stop at the earliest date that has a match.
            }
        }
        return null;
    }

    // True if every required facility is offered by the campsite OR by the plot.
    private boolean hasAllFacilities(Campsite campsite, Plot plot, MyLinkedList<String> required) {
        for (String facility : required) {
            boolean found = campsite.getFacilities().containsIgnoreCase(facility)
                    || plot.getFacilities().containsIgnoreCase(facility);
            if (!found) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------
    // Reports
    // ------------------------------------------------------------

    // Pending bookings for one campsite, grouped by camping area then plot.
    public String getPendingBookingsReport(Campsite campsite) {
        String report = "=== " + campsite.getName() + " ===\n";
        int count = 0;
        double total = 0;

        for (CampingArea area : campsite.getAreas()) {
            report += "  Area: " + area.getIdentifier() + "\n";
            for (Plot plot : area.getPlots()) {
                for (Booking b : plot.getBookings()) {
                    if (b.isPending()) {
                        report += "    Plot " + plot.getIdentifier() + ": " + b.getSummary() + "\n";
                        count++;
                        total += b.getTotalCost();
                    }
                }
            }
        }
        report += "  Total pending bookings: " + count
                + " | Total value: €" + String.format("%.2f", total) + "\n";
        return report;
    }

    // Pending bookings for the whole system, grouped by campsite, area and plot.
    public String getAllPendingBookingsReport() {
        String report = "";
        int count = 0;
        double total = 0;

        for (Campsite c : campsites) {
            report += getPendingBookingsReport(c) + "\n";
            // Add this campsite's figures to the overall totals.
            for (CampingArea area : c.getAreas()) {
                for (Plot plot : area.getPlots()) {
                    for (Booking b : plot.getBookings()) {
                        if (b.isPending()) {
                            count++;
                            total += b.getTotalCost();
                        }
                    }
                }
            }
        }
        report += "OVERALL pending bookings: " + count
                + " | Overall value: €" + String.format("%.2f", total);
        return report;
    }

    // A guest's full record, with pending and past bookings listed separately.
    public String getGuestRecord(Guest guest) {
        String record = "Guest #" + guest.getId() + ": " + guest.getName()
                + "\nDate of birth: " + guest.getDateOfBirth()
                + "\nAddress: " + guest.getAddress()
                + "\nPhone: " + guest.getPhone()
                + "\nEmail: " + guest.getEmail()
                + "\nOther info: " + guest.getExtraInfo();

        record += "\n\nPENDING BOOKINGS:\n";
        for (Booking b : guest.getBookings()) {
            if (b.isPending()) {
                record += b.getDetails() + "\n\n";
            }
        }

        record += "PAST BOOKINGS:\n";
        for (Booking b : guest.getBookings()) {
            if (!b.isPending()) {
                record += b.getDetails() + "\n\n";
            }
        }
        return record;
    }

    // ------------------------------------------------------------
    // Reset, save and load
    // ------------------------------------------------------------

    // Clears all data.
    public void reset() {
        campsites.clear();
        guests.clear();
        nextGuestId = 1;
        nextBookingId = 1;
    }


    public void save(String fileName) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
        out.writeObject(campsites);
        out.writeObject(guests);
        out.writeInt(nextGuestId);
        out.writeInt(nextBookingId);
        out.close();
    }

    // Loads everything back from the file, in the same order it was saved.
    @SuppressWarnings("unchecked")
    public void load(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        campsites = (MyLinkedList<Campsite>) in.readObject();
        guests = (MyLinkedList<Guest>) in.readObject();
        nextGuestId = in.readInt();
        nextBookingId = in.readInt();
        in.close();
    }

    public MyLinkedList<Campsite> getCampsites() { return campsites; }
    public MyLinkedList<Guest> getGuests() { return guests; }
}
