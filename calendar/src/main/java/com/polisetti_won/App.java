package com.polisetti_won;

import com.calendarfx.model.Entry;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.application.Platform;
import java.time.LocalDate;
import java.time.LocalTime;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        CalendarController calController = new CalendarController();
        Scene scene = new Scene(calController.getCalView());
        primaryStage.setTitle("Calendar");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1300);
        primaryStage.setHeight(1000);
        primaryStage.centerOnScreen();
        primaryStage.show();
        /*
        Entry<String> newEvent = new Entry<>("Test Event");
        newEvent.setId("1");
        newEvent.setFullDay(true);
        newEvent.setRecurrenceRule("FREQ=DAILY;COUNT=10");
        calController.holidays.addEntry(newEvent);
         */


    }

    public static void main(String[] args) {
            launch(args);
    }

}
