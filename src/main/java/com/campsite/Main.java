package com.campsite;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML layout file from the resources folder.
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
        Scene scene = new Scene(loader.load(), 1150, 780);

        stage.setTitle("Campsite Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();   // starts JavaFX, which then calls start().
    }
}
