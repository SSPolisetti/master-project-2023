/*
    Sasanka Polisetti
    AP CSA
    Cmdr. Schenk
    2nd Period
    27 April 2023
    Master Project - JavaFX Entry Point Class
*/
package com.polisetti_won;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainFx extends Application {

    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return MainFx.primaryStage;
    }

    private void setPrimaryStage(Stage stage) {
        MainFx.primaryStage = stage;
    }


    @Override
    public void start(Stage stage) throws Exception {
        // Init CalendarController and add it to the scene

        try {
            primaryStage = new Stage();

            setPrimaryStage(primaryStage);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/CalendarScene.fxml"));
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root);

//        CalendarController calController = new CalendarController();
//        Scene scene = new Scene(calController.getCalView());

            primaryStage.getIcons().add(new Image(MainFx.class.getResourceAsStream("/calendarIcon.png")));

            scene.getStylesheets().add(this.getClass().getResource("/style.css").toString());

            primaryStage.setTitle("Calendar");
            primaryStage.setScene(scene);
            primaryStage.setWidth(1300);
            primaryStage.setHeight(1000);
            primaryStage.centerOnScreen();
            primaryStage.show();



        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
