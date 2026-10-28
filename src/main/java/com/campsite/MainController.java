package com.campsite;

import com.campsite.datastructures.MyLinkedList;
import com.campsite.manager.CampsiteSystem;
import com.campsite.models.Booking;
import com.campsite.models.CampingArea;
import com.campsite.models.Campsite;
import com.campsite.models.Guest;
import com.campsite.models.Plot;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;


public class MainController {

    private static final String SAVE_FILE = "campsite-data.dat";

    // the object that holds all the data and logic.
    private CampsiteSystem system = new CampsiteSystem();

    // Remembers the unconfirmed result of the last smart find.
    private Booking smartFindResult = null;

    // Drill-down lists and details
    @FXML private ListView<Campsite> campsiteList;
    @FXML private ListView<CampingArea> areaList;
    @FXML private ListView<Plot> plotList;
    @FXML private ListView<Booking> bookingList;
    @FXML private TextArea detailsArea;

    // Add campsite
    @FXML private TextField campsiteNameField;
    @FXML private TextField campsiteAddressField;
    @FXML private TextField campsiteStarsField;
    @FXML private TextField campsiteContactField;
    @FXML private TextField campsiteFacilitiesField;

    // Add camping area
    @FXML private TextField areaIdField;
    @FXML private TextField areaDescriptionField;

    // Add plot
    @FXML private TextField plotIdField;
    @FXML private ComboBox<String> plotTypeBox;
    @FXML private CheckBox plotPowerCheck;
    @FXML private TextField plotBedroomsField;
    @FXML private TextField plotBedsField;
    @FXML private TextField plotSizeField;
    @FXML private TextField plotFacilitiesField;
    @FXML private TextField plotCostField;
    @FXML private TextField plotPhotoField;

    // Guests
    @FXML private TextField guestNameField;
    @FXML private DatePicker guestDobPicker;
    @FXML private TextField guestAddressField;
    @FXML private TextField guestPhoneField;
    @FXML private TextField guestEmailField;
    @FXML private TextField guestInfoField;
    @FXML private TextField viewGuestIdField;

    // Make booking
    @FXML private TextField bookingGuestIdField;
    @FXML private DatePicker bookingStartPicker;
    @FXML private DatePicker bookingEndPicker;
    @FXML private ComboBox<String> bookingTypeBox;
    @FXML private TextField bookingNumGuestsField;
    @FXML private CheckBox bookingPowerCheck;
    @FXML private TextField bookingMinSizeField;
    @FXML private TextField bookingMinBedroomsField;
    @FXML private TextField bookingBudgetField;

    // Smart find
    @FXML private TextField smartGuestIdField;
    @FXML private DatePicker smartStartPicker;
    @FXML private TextField smartNightsField;
    @FXML private TextField smartNumGuestsField;
    @FXML private TextField smartBudgetField;
    @FXML private TextField smartMinStarsField;
    @FXML private ComboBox<String> smartTypeBox;
    @FXML private CheckBox smartPowerCheck;
    @FXML private TextField smartFacilitiesField;


    @FXML
    public void initialize() {
        plotTypeBox.getItems().addAll(Plot.STATIC_MOBILE_HOME, Plot.BUNGALOW, Plot.APARTMENT, Plot.EMPTY_PITCH);
        bookingTypeBox.getItems().addAll("Any", Plot.STATIC_MOBILE_HOME, Plot.BUNGALOW, Plot.APARTMENT, Plot.EMPTY_PITCH);
        smartTypeBox.getItems().addAll("Any", Plot.STATIC_MOBILE_HOME, Plot.BUNGALOW, Plot.APARTMENT, Plot.EMPTY_PITCH);
        plotTypeBox.setValue(Plot.EMPTY_PITCH);
        bookingTypeBox.setValue("Any");
        smartTypeBox.setValue("Any");
    }

    // ============================================================
    // Drill-down: clicking a list shows the next level down.
    // ============================================================

    // Reloads the campsite list from the system and empties the lower lists.
    private void refreshCampsites() {
        campsiteList.getItems().clear();
        for (Campsite c : system.getCampsites()) {
            campsiteList.getItems().add(c);
        }
        areaList.getItems().clear();
        plotList.getItems().clear();
        bookingList.getItems().clear();
    }

    private void showAreas(Campsite campsite) {
        areaList.getItems().clear();
        plotList.getItems().clear();
        bookingList.getItems().clear();
        for (CampingArea a : campsite.getAreas()) {
            areaList.getItems().add(a);
        }
    }

    private void showPlots(CampingArea area) {
        plotList.getItems().clear();
        bookingList.getItems().clear();
        for (Plot p : area.getPlots()) {
            plotList.getItems().add(p);
        }
    }

    private void showBookings(Plot plot) {
        bookingList.getItems().clear();
        for (Booking b : plot.getBookings()) {
            bookingList.getItems().add(b);
        }
    }

    @FXML
    public void campsiteClicked() {
        Campsite c = campsiteList.getSelectionModel().getSelectedItem();
        if (c != null) {
            showAreas(c);
            detailsArea.setText(c.getDetails());
        }
    }

    @FXML
    public void areaClicked() {
        CampingArea a = areaList.getSelectionModel().getSelectedItem();
        if (a != null) {
            showPlots(a);
            detailsArea.setText(a.getDetails());
        }
    }

    @FXML
    public void plotClicked() {
        Plot p = plotList.getSelectionModel().getSelectedItem();
        if (p != null) {
            showBookings(p);
            detailsArea.setText(p.getDetails());
        }
    }

    @FXML
    public void bookingClicked() {
        Booking b = bookingList.getSelectionModel().getSelectedItem();
        if (b != null) {
            detailsArea.setText(b.getDetails());
        }
    }

    // ============================================================
    // Adding things.
    // ============================================================

    @FXML
    public void addCampsite() {
        try {
            String name = campsiteNameField.getText().trim();
            if (name.isEmpty()) {
                showMessage("Please enter a campsite name.");
                return;
            }
            int stars = Integer.parseInt(campsiteStarsField.getText().trim());
            MyLinkedList<String> facilities = MyLinkedList.fromCommaText(campsiteFacilitiesField.getText());

            Campsite c = new Campsite(name, campsiteAddressField.getText(), stars,
                    campsiteContactField.getText(), facilities);

            if (system.addCampsite(c)) {
                refreshCampsites();
                showMessage("Campsite added.");
            } else {
                showMessage("A campsite with that name already exists.");
            }
        } catch (NumberFormatException e) {
            showMessage("Stars must be a whole number.");
        }
    }

    @FXML
    public void addCampingArea() {
        Campsite c = campsiteList.getSelectionModel().getSelectedItem();
        if (c == null) {
            showMessage("Select a campsite in the list first.");
            return;
        }
        String id = areaIdField.getText().trim();
        if (id.isEmpty()) {
            showMessage("Please enter an area identifier.");
            return;
        }

        CampingArea area = new CampingArea(id, areaDescriptionField.getText());
        if (system.addCampingArea(c, area)) {
            showAreas(c);
            showMessage("Camping area added to " + c.getName() + ".");
        } else {
            showMessage("That identifier is already used in this campsite.");
        }
    }

    @FXML
    public void addPlot() {
        CampingArea area = areaList.getSelectionModel().getSelectedItem();
        if (area == null) {
            showMessage("Select a camping area in the list first.");
            return;
        }
        try {
            String id = plotIdField.getText().trim();
            if (id.isEmpty()) {
                showMessage("Please enter a plot identifier.");
                return;
            }
            int bedrooms = readIntOrZero(plotBedroomsField);
            int beds = readIntOrZero(plotBedsField);
            double size = Double.parseDouble(plotSizeField.getText().trim());
            double cost = Double.parseDouble(plotCostField.getText().trim());
            MyLinkedList<String> facilities = MyLinkedList.fromCommaText(plotFacilitiesField.getText());

            Plot plot = new Plot(id, plotTypeBox.getValue(), plotPowerCheck.isSelected(),
                    bedrooms, beds, size, facilities, cost, plotPhotoField.getText());

            if (system.addPlot(area, plot)) {
                showPlots(area);
                showMessage("Plot added to " + area.getIdentifier() + ".");
            } else {
                showMessage("That plot identifier is already used in this area.");
            }
        } catch (NumberFormatException e) {
            showMessage("Please enter valid numbers for size and cost.");
        }
    }

    @FXML
    public void addGuest() {
        String name = guestNameField.getText().trim();
        if (name.isEmpty() || guestDobPicker.getValue() == null) {
            showMessage("Please enter a name and date of birth.");
            return;
        }
        Guest g = system.addGuest(name, guestDobPicker.getValue(), guestAddressField.getText(),
                guestPhoneField.getText(), guestEmailField.getText(), guestInfoField.getText());
        showMessage("Guest added with ID " + g.getId() + ".");
    }

    @FXML
    public void viewGuestRecord() {
        Guest g = readGuest(viewGuestIdField);
        if (g != null) {
            detailsArea.setText(system.getGuestRecord(g));
        }
    }

    // ============================================================
    // Bookings.
    // ============================================================

    @FXML
    public void makeBooking() {
        Campsite c = campsiteList.getSelectionModel().getSelectedItem();
        CampingArea area = areaList.getSelectionModel().getSelectedItem();
        if (c == null || area == null) {
            showMessage("Select a campsite and a camping area in the lists first.");
            return;
        }
        Guest g = readGuest(bookingGuestIdField);
        if (g == null) {
            return;
        }
        LocalDate start = bookingStartPicker.getValue();
        LocalDate end = bookingEndPicker.getValue();
        if (start == null || end == null || !end.isAfter(start)) {
            showMessage("Please choose a start date and a later end date.");
            return;
        }
        try {
            int numGuests = Integer.parseInt(bookingNumGuestsField.getText().trim());
            double minSize = readDoubleOrZero(bookingMinSizeField);
            int minBedrooms = readIntOrZero(bookingMinBedroomsField);
            double budget = Double.parseDouble(bookingBudgetField.getText().trim());

            Booking b = system.makeBooking(c, area, g, start, end, bookingTypeBox.getValue(),
                    numGuests, bookingPowerCheck.isSelected(), minSize, minBedrooms, budget);

            if (b == null) {
                showMessage("Sorry, no suitable plot/pitch is available in " + area.getIdentifier() + ".");
            } else {
                showPlots(area);
                detailsArea.setText("Booking made!\n\n" + b.getDetails());
            }
        } catch (NumberFormatException e) {
            showMessage("Please enter valid numbers for guests and budget.");
        }
    }

    @FXML
    public void cancelBooking() {
        Booking b = bookingList.getSelectionModel().getSelectedItem();
        if (b == null) {
            showMessage("Select a booking in the bookings list first.");
            return;
        }
        system.cancelBooking(b);
        showBookings(b.getPlot());
        detailsArea.setText("Booking #" + b.getId() + " cancelled.");
    }

    @FXML
    public void removePlot() {
        Campsite c = campsiteList.getSelectionModel().getSelectedItem();
        CampingArea area = areaList.getSelectionModel().getSelectedItem();
        Plot plot = plotList.getSelectionModel().getSelectedItem();
        if (c == null || area == null || plot == null) {
            showMessage("Select a campsite, area and plot in the lists first.");
            return;
        }

        MyLinkedList<Booking> notMoved = system.removePlot(c, area, plot);
        showPlots(area);

        String message = "Plot " + plot.getIdentifier() + " removed.";
        if (notMoved.isEmpty()) {
            message += "\nAll pending bookings were moved (if there were any).";
        } else {
            message += "\nThese bookings could not be moved and were cancelled:";
            for (Booking b : notMoved) {
                message += "\n  " + b;
            }
        }
        detailsArea.setText(message);
    }

    // ============================================================
    // Smart find.
    // ============================================================

    @FXML
    public void smartFind() {
        Guest g = readGuest(smartGuestIdField);
        if (g == null) {
            return;
        }
        if (smartStartPicker.getValue() == null) {
            showMessage("Please choose a desired start date.");
            return;
        }
        try {
            int nights = Integer.parseInt(smartNightsField.getText().trim());
            int numGuests = Integer.parseInt(smartNumGuestsField.getText().trim());
            double budget = Double.parseDouble(smartBudgetField.getText().trim());
            int minStars = readIntOrZero(smartMinStarsField);
            MyLinkedList<String> facilities = MyLinkedList.fromCommaText(smartFacilitiesField.getText());

            smartFindResult = system.smartFind(g, smartStartPicker.getValue(), nights, numGuests,
                    budget, minStars, smartTypeBox.getValue(), smartPowerCheck.isSelected(), facilities);

            if (smartFindResult == null) {
                detailsArea.setText("Smart find: nothing suitable within 14 days of that date.");
            } else {
                detailsArea.setText("Smart find suggestion (press 'Book It' to confirm):\n\n"
                        + smartFindResult.getDetails());
            }
        } catch (NumberFormatException e) {
            showMessage("Please enter valid numbers for nights, guests, budget and stars.");
        }
    }

    @FXML
    public void bookSmartFind() {
        if (smartFindResult == null) {
            showMessage("Run a smart find first.");
            return;
        }
        system.confirmBooking(smartFindResult);
        detailsArea.setText("Booking made!\n\n" + smartFindResult.getDetails());
        smartFindResult = null;
        refreshCampsites();
    }

    // ============================================================
    // Reports.
    // ============================================================

    @FXML
    public void showCampsitePending() {
        Campsite c = campsiteList.getSelectionModel().getSelectedItem();
        if (c == null) {
            showMessage("Select a campsite first.");
            return;
        }
        detailsArea.setText(system.getPendingBookingsReport(c));
    }

    @FXML
    public void showAllPending() {
        detailsArea.setText(system.getAllPendingBookingsReport());
    }

    // ============================================================
    // Save, load, reset.
    // ============================================================

    @FXML
    public void save() {
        try {
            system.save(SAVE_FILE);
            showMessage("Data saved to " + SAVE_FILE);
        } catch (Exception e) {
            showMessage("Error saving: " + e.getMessage());
        }
    }

    @FXML
    public void load() {
        try {
            system.load(SAVE_FILE);
            refreshCampsites();
            showMessage("Data loaded.");
        } catch (Exception e) {
            showMessage("Error loading: " + e.getMessage());
        }
    }

    @FXML
    public void reset() {
        system.reset();
        refreshCampsites();
        detailsArea.clear();
        showMessage("All data cleared.");
    }

    // ============================================================
    // Small helper methods.
    // ============================================================

    // Shows a pop-up message box.
    private void showMessage(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    // Reads a whole number from a text field, or returns 0 if the field is blank.
    private int readIntOrZero(TextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(text);
    }

    // Reads a decimal number from a text field, or returns 0 if the field is blank.
    private double readDoubleOrZero(TextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return 0;
        }
        return Double.parseDouble(text);
    }

    // Reads a guest id from a text field and finds the guest. Shows a message if not found.
    private Guest readGuest(TextField field) {
        try {
            int id = Integer.parseInt(field.getText().trim());
            Guest g = system.findGuest(id);
            if (g == null) {
                showMessage("No guest with ID " + id + ".");
            }
            return g;
        } catch (NumberFormatException e) {
            showMessage("Guest ID must be a whole number.");
            return null;
        }
    }
}
