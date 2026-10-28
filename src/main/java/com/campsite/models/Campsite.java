package com.campsite.models;

import com.campsite.datastructures.MyLinkedList;

import java.io.Serializable;

/**
 * A campsite, e.g. "Sunny Shores Holiday Park".
 * A campsite holds a list of camping areas.
 */
public class Campsite implements Serializable {

    private String name;
    private String address;
    private int stars;
    private String contactInfo;
    private MyLinkedList<String> facilities;      // e.g. Restaurant, WiFi, Laundry
    private MyLinkedList<CampingArea> areas;      // the camping areas in this campsite

    public Campsite(String name, String address, int stars, String contactInfo, MyLinkedList<String> facilities) {
        this.name = name;
        this.address = address;
        this.stars = stars;
        this.contactInfo = contactInfo;
        this.facilities = facilities;
        this.areas = new MyLinkedList<>();
    }

    /**
     * Adds a camping area, but only if no other area here has the same identifier.
     * Returns true if it was added, false if the identifier was already used.
     */
    public boolean addArea(CampingArea area) {
        if (findArea(area.getIdentifier()) != null) {
            return false;   // duplicate identifier, so refuse
        }
        areas.add(area);
        return true;
    }

    // Finds an area by its identifier (ignoring case). Returns null if not found
    public CampingArea findArea(String identifier) {
        for (CampingArea area : areas) {
            if (area.getIdentifier().equalsIgnoreCase(identifier)) {
                return area;
            }
        }
        return null;
    }

    // Getters
    public String getName() { return name; }
    public String getAddress() { return address; }
    public int getStars() { return stars; }
    public String getContactInfo() { return contactInfo; }
    public MyLinkedList<String> getFacilities() { return facilities; }
    public MyLinkedList<CampingArea> getAreas() { return areas; }

    // Full details, shown in the details box of the GUI
    public String getDetails() {
        return "Campsite: " + name
                + "\nAddress: " + address
                + "\nStars: " + stars
                + "\nContact: " + contactInfo
                + "\nFacilities: " + facilities
                + "\nCamping areas: " + areas.size();
    }

    // What the ListView shows for this object
    @Override
    public String toString() {
        return name + " (" + stars + " stars)";
    }
}
