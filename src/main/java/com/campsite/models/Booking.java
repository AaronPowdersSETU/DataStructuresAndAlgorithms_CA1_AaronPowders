package com.campsite.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public class Booking implements Serializable {

    private int id;
    private Guest guest;
    private LocalDate startDate;   // arrival day
    private LocalDate endDate;     // check-out day

    // Booking preferences.
    private String preferredType;  // e.g. "Bungalow" or "Empty Pitch"
    private int numberOfGuests;
    private boolean needsPower;
    private double minSize;
    private int minBedrooms;
    private double budget;         // maximum total cost for the whole stay

    // Where the booking ended up (filled in once a plot is found)
    private Campsite campsite;
    private CampingArea area;
    private Plot plot;

    public Booking(int id, Guest guest, LocalDate startDate, LocalDate endDate, String preferredType,
                   int numberOfGuests, boolean needsPower, double minSize, int minBedrooms, double budget) {
        this.id = id;
        this.guest = guest;
        this.startDate = startDate;
        this.endDate = endDate;
        this.preferredType = preferredType;
        this.numberOfGuests = numberOfGuests;
        this.needsPower = needsPower;
        this.minSize = minSize;
        this.minBedrooms = minBedrooms;
        this.budget = budget;
    }

    // Number of nights.
    public long getNights() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    // Total price for the stay (0 if no plot has been assigned yet)
    public double getTotalCost() {
        if (plot == null) {
            return 0;
        }
        return getNights() * plot.getCostPerNight();
    }

    // A booking is "pending" if the stay has not finished yet.
    public boolean isPending() {
        return !endDate.isBefore(LocalDate.now());
    }

    // Links this booking to the place it was assigned.
    public void assignTo(Campsite campsite, CampingArea area, Plot plot) {
        this.campsite = campsite;
        this.area = area;
        this.plot = plot;
    }

    public int getId() { return id; }
    public Guest getGuest() { return guest; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getPreferredType() { return preferredType; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public boolean needsPower() { return needsPower; }
    public double getMinSize() { return minSize; }
    public int getMinBedrooms() { return minBedrooms; }
    public double getBudget() { return budget; }
    public Campsite getCampsite() { return campsite; }
    public CampingArea getArea() { return area; }
    public Plot getPlot() { return plot; }

    // One line summary used in reports.
    public String getSummary() {
        return "Booking #" + id
                + " | Guest: " + guest.getName()
                + " | " + startDate + " to " + endDate + " (" + getNights() + " nights)"
                + " | Guests: " + numberOfGuests
                + " | Cost: €" + String.format("%.2f", getTotalCost());
    }

    public String getDetails() {
        return getSummary()
                + "\nCampsite: " + (campsite == null ? "-" : campsite.getName())
                + "\nArea: " + (area == null ? "-" : area.getIdentifier())
                + "\nPlot: " + (plot == null ? "-" : plot.getIdentifier())
                + "\nStatus: " + (isPending() ? "Pending" : "Past");
    }

    @Override
    public String toString() {
        return "#" + id + " " + guest.getName() + " (" + startDate + " to " + endDate + ")";
    }
}
