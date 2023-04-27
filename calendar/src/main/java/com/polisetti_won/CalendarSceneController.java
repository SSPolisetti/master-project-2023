/*
    Sasanka Polisetti, Tillman Won
    AP CSA
    Cmdr. Schenk
    2nd Period
    27 April 2023
    Master Project - Scene Controller
*/
package com.polisetti_won;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXML;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.ResourceBundle;

public class CalendarSceneController implements Initializable {

    public Stage myStage;

    //CalendarFX + Javafx fields
    @FXML
    private CalendarView calView;
    @FXML
    private VBox myVBox;
    @FXML
    private MenuItem menuItemDay;
    @FXML
    private MenuItem menuItemWeek;
    @FXML
    private MenuItem menuItemMonth;
    @FXML
    private MenuItem menuItemYear;


    //Fields
    private Calendar<CalendarEvent> personal = new Calendar<>("Personal");
    private Calendar<CalendarEvent> birthdays = new Calendar<>("Birthdays");
    private Calendar<CalendarEvent> holidays = new Calendar<>("Holidays");

    //private Calendar<CalendarEvent> tasks = new Calendar<>("Tasks");



    // Database Object
    CalendarDb dbManager = new CalendarDb();

//    @FXML
//    public void btnConnectClicked(ActionEvent event) {
//        dbManager.connect();
//        dbManager.loadEvents();
//    }

//    @FXML
//    public void btnDisconnectClicked(ActionEvent event) {
//        dbManager.disconnect();
//    }


    //Initialization function to lock GUI, add  calendars to calendar view, and start  background thread to update time
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Starting on the Month Page view
        calView.showMonthPage();

        //calView.setBackground(new Background(new BackgroundImage(new Image("background.png"), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));

        // Hiding unused controls
        this.calView.setShowPrintButton(false);
        this.calView.setShowPageSwitcher(false);
        this.calView.setShowAddCalendarButton(false);
        this.calView.setShowDeveloperConsole(false);
        //this.calView.setShowSearchField(false);


        // Styling Events of Each Calendar
        this.personal.setStyle(Calendar.Style.STYLE3);
        this.birthdays.setStyle(Calendar.Style.STYLE1);
        this.holidays.setStyle(Calendar.Style.STYLE2);
        //this.tasks.setStyle(Calendar.Style.STYLE4);
        this.calView.getCalendarSources().remove(0);
        System.out.println(this.calView.getCalendarSources());

        // Adding Calendars to Calendar View and setting current internal time to current LocalTime
        CalendarSource myCalendarSource = new CalendarSource("My Calendar");
        myCalendarSource.getCalendars().addAll(this.personal, this.birthdays, this.holidays);
        this.calView.getCalendarSources().addAll(myCalendarSource);
        this.calView.setRequestedTime(LocalTime.now());

        System.out.println(this.calView.getCalendarSources());


        // Locking the GUI until the user connects to db
        disableControls();



        // Background thread to update time

        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
                        calView.setToday(LocalDate.now());
                        calView.setTime(LocalTime.now());
                    });
                    try {
                        // update every 10 seconds
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();

        // Calendar Event Handler Bindings

        EventHandler<CalendarEvent> onAdd = this::addEvent;
        EventHandler<CalendarEvent> onUpdate = this::updateEvent;
        EventHandler<CalendarEvent> onDelete = this::deleteEvent;

        this.personal.addEventHandler(onAdd);
        this.personal.addEventHandler(onUpdate);
        this.personal.addEventHandler(onDelete);
        this.birthdays.addEventHandler(onAdd);
        this.birthdays.addEventHandler(onUpdate);
        this.birthdays.addEventHandler(onDelete);
        this.holidays.addEventHandler(onAdd);
        this.holidays.addEventHandler(onUpdate);
        this.holidays.addEventHandler(onDelete);
//        this.tasks.addEventHandler(onAdd);
//        this.tasks.addEventHandler(onUpdate);
//        this.tasks.addEventHandler(onDelete);

    }

    // Event Handlers
    // Used to access methods of Database object

    // Tillman - Insert New Event
    public void addEvent(CalendarEvent evt) {

        System.out.println(evt.getEventType() + "\n");

        if (evt.getEventType() == CalendarEvent.ENTRY_CALENDAR_CHANGED && evt.getEntry().getId().length() > 8) {

            Entry<String> newEvent = (Entry<String>) evt.getEntry();
            System.out.println(newEvent.getTitle() + "\n");

            CalendarRecord newRecord = new CalendarRecord(newEvent.getTitle(), newEvent.getCalendar().getName(), newEvent.getInterval().getStartDateTime(), newEvent.getInterval().getEndDateTime(), newEvent.getRecurrenceRule(), newEvent.isFullDay(),newEvent.getLocation());

            dbManager.addEvent(newEvent, newRecord);


        }
    }

    // Sasanka - Update Existing Event
    public void updateEvent(CalendarEvent evt) {

        Entry<String> newEvent = (Entry<String>) evt.getEntry();

        dbManager.updateEvent(evt, newEvent);



    }

    // Sasanka - Delete existing event
    public void deleteEvent(CalendarEvent evt) {

        if (evt.getEntry().getCalendar() == null) {

            dbManager.deleteEvent(Integer.parseInt(evt.getEntry().getId()));

        }

    }

    // Callback Functions

    // Tillman - Help About Popup for About Menu Button
    @FXML
    public void menuAboutClicked(ActionEvent event) {

        String header = "Calendar Database App";
        String content = "Calendar App for tracking schedule developed by Tillman Won and Sasanka Polisetti";
        Alert.AlertType type = Alert.AlertType.INFORMATION;
        Alert alert = new Alert(type, "");
        alert.initModality((Modality.APPLICATION_MODAL));
        Stage stage = (Stage) myVBox.getScene().getWindow();
        stage.setTitle("About");
        alert.initOwner(stage);
        alert.getDialogPane().setHeaderText(header);
        alert.getDialogPane().setContentText(content);
        alert.showAndWait();

    }

    // Sasanka - Disconnect Button in File Menu
    @FXML
    public void menuDisconnectClicked(ActionEvent event) {

        dbManager.disconnect();
        disableControls();

    }

    // Sasanka - Connect Button in File Menu
    @FXML
    public void menuConnectClicked(ActionEvent event) {

        dbManager.connect();
        for (CalendarRecord record : dbManager.getResults()) {
            Entry<String> newEntry = record.createEntry();

            if (Objects.equals(record.getCalendar(),"Personal")) {
                this.personal.addEntry(newEntry);
            } else if (Objects.equals(record.getCalendar(),"Birthdays")) {
                this.birthdays.addEntry(newEntry);
            } else if (Objects.equals(record.getCalendar(),"Holidays")) {
                this.holidays.addEntry(newEntry);
            }

        }

        enableControls();


    }

    // Sasanka - Exit Button in File Menu
    @FXML
    public void menuExitClicked(ActionEvent event) {

        Platform.exit();

    }

    // Sasanka - Switch to Day Page view
    @FXML
    public void menuDayClicked(ActionEvent event) {

        calView.showDayPage();

    }

    // Sasanka - Switch to Week Page view
    @FXML
    public void menuWeekClicked(ActionEvent event) {

        calView.showWeekPage();

    }

    // Sasanka - Switch to Month Page view
    @FXML
    public void menuMonthClicked(ActionEvent event) {

        calView.showMonthPage();

    }

    // Sasanka - Switch to Year Page view
    @FXML
    public void menuYearClicked(ActionEvent event) {

        calView.showYearPage();

    }
    // Sasanka - GUI Locking and Unlocking Methods
    public void enableControls() {

        calView.setDisable(false);
        menuItemDay.setDisable(false);
        menuItemWeek.setDisable(false);
        menuItemMonth.setDisable(false);
        menuItemYear.setDisable(false);

    }
    public void disableControls() {

        calView.setDisable(true);
        menuItemDay.setDisable(true);
        menuItemWeek.setDisable(true);
        menuItemMonth.setDisable(true);
        menuItemYear.setDisable(true);

    }

}


