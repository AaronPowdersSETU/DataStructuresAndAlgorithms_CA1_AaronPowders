package com.campsite.models;

import com.campsite.datastructures.MyLinkedList;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A single plot/pitch inside a camping area.
 * It can be a static dwelling (mobile home, bungalow, apartment) or an empty pitch.
 * Each plot keeps its own list of bookings.
 */
public class Plot implements Serializable {

    // The plot types used throughout the system
    public static final String STATIC_MOBILE_HOME = "Static Mobile Home";
    public static final String BUNGALOW = "Bungalow";
    public static final String APARTMENT = "Apartment";
    public static final String EMPTY_PITCH = "Empty Pitch";

    private String identifier;
    private String type;
    private boolean hasPower;
    private int bedrooms;
    private int beds;
    private double size;                       // square metres
    private MyLinkedList<String> facilities;   // e.g. Air Conditioning, Hot Tub
    private double costPerNight;               // euro
    private String photoUrl;
    private MyLinkedList<Booking> bookings;    // bookings made for this plot

    public Plot(String identifier, String type, boolean hasPower, int bedrooms, int beds,
                double size, MyLinkedList<String> facilities, double costPerNight, String photoUrl) {
        this.identifier = identifier;
        this.type = type;
        this.size = size;
        this.facilities = facilities;
        this.costPerNight = costPerNight;
        this.photoUrl = photoUrl;
        this.bookings = new MyLinkedList<>();

        if (isStatic()) {
            // The brief says static dwellings always have power
            this.hasPower = true;
            this.bedrooms = bedrooms;
            this.beds = beds;
        } else {
            // Empty pitches: power is optional, and there are no bedrooms or beds
            this.hasPower = hasPower;
            this.bedrooms = 0;
            this.beds = 0;
        }
    }

    // A plot is "static" if it is anything other than an empty pitch
    public boolean isStatic() {
        return !type.equalsIgnoreCase(EMPTY_PITCH);
    }

    /**
     * Checks if this plot is free between two dates.
     * Two stays overlap if one starts before the other ends.
     * The end date is the check-out day, so a new guest can arrive that same day.
     */
    public boolean isAvailable(LocalDate start, LocalDate end) {
        for (Booking b : bookings) {
            boolean overlaps = start.isBefore(b.getEndDate()) && b.getStartDate().isBefore(end);
            if (overlaps) {
                return false;
            }
        }
        return true;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    public boolean removeBooking(Booking booking) {
        return bookings.remove(booking);
    }

    public String getIdentifier() { return identifier; }
    public String getType() { return type; }
    public boolean hasPower() { return hasPower; }
    public int getBedrooms() { return bedrooms; }
    public int getBeds() { return beds; }
    public double getSize() { return size; }
    public MyLinkedList<String> getFacilities() { return facilities; }
    public double getCostPerNight() { return costPerNight; }
    public String getPhotoUrl() { return photoUrl; }
    public MyLinkedList<Booking> getBookings() { return bookings; }

    public String getDetails() {
        return "Plot/pitch: " + identifier
                + "\nType: " + type
                + "\nPower: " + (hasPower ? "Yes" : "No")
                + "\nBedrooms: " + bedrooms + ", Beds: " + beds
                + "\nSize: " + size + " m2"
                + "\nFacilities: " + facilities
                + "\nCost per night: €" + String.format("%.2f", costPerNight)
                + "\nPhoto: " + photoUrl
                + "\nBookings: " + bookings.size();
    }

    @Override
    public String toString() {
        return identifier + " - " + type + " (€" + String.format("%.2f", costPerNight) + "/night)";
    }
}
