package com.campsite.models;

import com.campsite.datastructures.MyLinkedList;

import java.io.Serializable;


public class CampingArea implements Serializable {

    private String identifier;
    private String description;
    private MyLinkedList<Plot> plots;

    public CampingArea(String identifier, String description) {
        this.identifier = identifier;
        this.description = description;
        this.plots = new MyLinkedList<>();
    }


    public boolean addPlot(Plot plot) {
        if (findPlot(plot.getIdentifier()) != null) {
            return false;
        }
        plots.add(plot);
        return true;
    }

    // Finds a plot by identifier (ignoring case). Returns null if not found.
    public Plot findPlot(String identifier) {
        for (Plot plot : plots) {
            if (plot.getIdentifier().equalsIgnoreCase(identifier)) {
                return plot;
            }
        }
        return null;
    }

    // Removes a plot from this area.
    public boolean removePlot(Plot plot) {
        return plots.remove(plot);
    }

    public String getIdentifier() { return identifier; }
    public String getDescription() { return description; }
    public MyLinkedList<Plot> getPlots() { return plots; }

    public String getDetails() {
        return "Camping area: " + identifier
                + "\nDescription: " + description
                + "\nPlots/pitches: " + plots.size();
    }

    @Override
    public String toString() {
        return identifier;
    }
}
