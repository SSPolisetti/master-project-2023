/*
    Tillman Won, Sasanka Polisetti
    AP CSA
    Cmdr. Schenk
    2nd Period
    27 April 2023
    Master Project Entry Point
*/

package com.polisetti_won;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Init CalendarController and add it to the scene
        CalendarController calController = new CalendarController();
        Scene scene = new Scene(calController.getCalView());
        primaryStage.setTitle("Calendar");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1300);
        primaryStage.setHeight(1000);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
            launch(args);
    }

}
