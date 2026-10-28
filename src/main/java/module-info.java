// Tells Java which libraries this project needs and which packages other modules may use
module com.campsite {
    requires javafx.controls;
    requires javafx.fxml;

    // FXML needs to "open" the package so it can inject @FXML fields into the controller
    opens com.campsite to javafx.fxml;

    exports com.campsite;
    exports com.campsite.datastructures;
    exports com.campsite.models;
    exports com.campsite.manager;
}
