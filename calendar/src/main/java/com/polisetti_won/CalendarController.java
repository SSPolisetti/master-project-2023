/*
    Tillman Won, Sasanka Polisetti
    AP CSA
    Cmdr. Schenk
    2nd Period
    27 April 2023
    Master Project - DB Class and Controller/Container Class for Calendar Sources
*/
/*
    NOTE:
    This class is the controller for the CalendarFX CalendarView and our MySQL DB. CalendarFX is a
    framework for JavaFX that provides a useful GUI class for creating our calendar. CalendarFX allows us
    to add event handlers for the CalendarView that will trigger the CRUD Operations written in this
    class.

    This class is responsible for connecting to the MySQL database, loading events from the database,
    and updating the database when calendar events are added, changed, or deleted.
*/

package com.polisetti_won;
// JavaFX and CalendarFX
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.event.EventHandler;
// MySQL
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class CalendarController {

    // Fields
    public CalendarView calendarView;
    private ArrayList<CalendarRecord> calendarRecords;
    private Calendar personal;
    private Calendar birthdays;
    private Calendar holidays;
    private Connection conn;
    private ResultSet rs;

    // Tillman and Sasanka - Contructor
    public CalendarController() {
        // Init Calendar GUI from CalendarFX
        this.calendarView = new CalendarView();
        // Add Calendars we are tracking
        this.personal = new Calendar("Personal");
        this.birthdays = new Calendar("Birthdays");
        this.holidays = new Calendar("Holidays");
        // Set Calendar Styles
        this.personal.setStyle(Calendar.Style.STYLE3);
        this.birthdays.setStyle(Calendar.Style.STYLE1);
        this.holidays.setStyle(Calendar.Style.STYLE2);
        this.calendarView.getCalendarSources().remove(0);
        System.out.println(this.calendarView.getCalendarSources());
        // Add Calendars to Calendar View
        CalendarSource myCalendarSource = new CalendarSource("My Calendars");
        myCalendarSource.getCalendars().addAll(this.personal, this.birthdays, this.holidays);
        this.calendarView.getCalendarSources().addAll(myCalendarSource); // (5)
        this.calendarView.setRequestedTime(LocalTime.now());
        // Connect DB and Load Events
        connectDB();
        // Background Thread to update the time to device's system time
        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
                        calendarView.setToday(LocalDate.now());
                        calendarView.setTime(LocalTime.now());
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

        // Calendar Bindings
        EventHandler<CalendarEvent> onAdd = evt -> addEvent(evt);
        EventHandler<CalendarEvent> onChange = evt -> updateEvent(evt);
        EventHandler<CalendarEvent> onDelete = evt -> deleteEvent(evt);
        this.personal.addEventHandler(onAdd);
        this.personal.addEventHandler(onChange);
        this.personal.addEventHandler(onDelete);
        this.birthdays.addEventHandler(onAdd);
        this.birthdays.addEventHandler(onChange);
        this.birthdays.addEventHandler(onDelete);
        this.holidays.addEventHandler(onAdd);
        this.holidays.addEventHandler(onChange);
        this.holidays.addEventHandler(onDelete);

    }
    // Tillman - Connect to MySQL database
    public boolean connectDB() {
        try {
            this.conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/calendar", "root", "Testing123!!");
            System.out.println("Connected");
            loadEvents();
        }catch(Exception e) {
            System.out.println("Error Connecting:");
            System.out.println(e);
            return false;
        }
        return true;
    }

    // Tillman - CREATE - event was created
    public void addEvent(CalendarEvent evt) {
        System.out.println(evt.getEventType());
        if (evt.getEventType() == CalendarEvent.ENTRY_CALENDAR_CHANGED && evt.getEntry().getId().length() > 8) {
            System.out.println("Entry created");
            Entry<String> newEvent = (Entry<String>) evt.getEntry();
            System.out.println(newEvent.getTitle());
            System.out.println();
            // Add to DB
            // Create CalendarRecord object
            CalendarRecord newRecord = new CalendarRecord(Integer.parseInt(newEvent.getId()), newEvent.getTitle(), newEvent.getCalendar().getName(), newEvent.getInterval().getStartDateTime(), newEvent.getInterval().getEndDateTime(), newEvent.getRecurrenceRule(), newEvent.isFullDay());
            // Add to ArrayList
            this.calendarRecords.add(newRecord);
            // SQL Insertion
            try {
                // Prepare SQL Statement
                String sql = "INSERT INTO events (title, calendar, start_datetime, end_datetime, recurrence, full_day) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = this.conn.prepareStatement(sql);
                stmt.setString(1, newEvent.getTitle());
                switch (newEvent.getCalendar().getName()) {
                    case "Personal":
                        stmt.setString(2, "DEFAULT");
                        break;
                    case "Birthdays":
                        stmt.setString(2, "BIRTHDAYS");
                        break;
                    case "Holidays":
                        stmt.setString(2, "HOLIDAYS");
                        break;
                    default:
                        stmt.setString(2, "DEFAULT");
                        break;
                }
                stmt.setString(3, newEvent.getInterval().getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                stmt.setString(4, newEvent.getInterval().getEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                stmt.setString(5, newEvent.getRecurrenceRule());
                stmt.setShort(6, (short) (newEvent.isFullDay() ? 1 : 0));
                // Execute SQL Statement
                stmt.executeUpdate();
                // Get ID of new entry
                rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
                rs.next();
                newEvent.setId(String.valueOf(rs.getInt(1)));
                System.out.println(newEvent.getId());
            } catch(Exception e) {
                System.out.println("Error Creating Entry:");
                System.out.println(e);
            }
        }
    }

    // Tillman - READ - Load events from DB into calendar
    public void loadEvents() {
        // Load Events from DB
        try {
            Statement stmt = this.conn.createStatement();
            rs = stmt.executeQuery("SELECT * from events");
            // For each event in the DB...
            while(rs.next()) {
                // If it's a personal event, add it to the personal calendar
                if (Objects.equals(rs.getString(3), "DEFAULT")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime startDate = LocalDateTime.parse(rs.getString(4), formatter);
                    LocalDateTime endDate = LocalDateTime.parse(rs.getString(5), formatter);
                    Entry<String> newEvent = new Entry<>(rs.getString(2));
                    newEvent.setId(String.valueOf(rs.getInt(1)));
                    newEvent.setInterval(startDate, endDate);
                    newEvent.setFullDay((rs.getShort(7) == 1));
                    newEvent.setRecurrenceRule(rs.getString(6));
                    this.personal.addEntry(newEvent);
                    // If it's a birthday add it to the birthdays calendar
                } else if (Objects.equals(rs.getString(3), "BIRTHDAYS")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime startDate = LocalDateTime.parse(rs.getString(4), formatter);
                    LocalDateTime endDate = LocalDateTime.parse(rs.getString(5), formatter);
                    Entry<String> newEvent = new Entry<>(rs.getString(2));
                    newEvent.setId(String.valueOf(rs.getInt(1)));
                    newEvent.setInterval(startDate, endDate);
                    newEvent.setFullDay((rs.getShort(7) == 1));
                    newEvent.setRecurrenceRule("FREQ=YEARLY");
                    this.birthdays.addEntry(newEvent);
                    // If it's a holiday, add it to the holidays calendar
                } else if (Objects.equals(rs.getString(3), "HOLIDAYS")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime startDate = LocalDateTime.parse(rs.getString(4), formatter);
                    LocalDateTime endDate = LocalDateTime.parse(rs.getString(5), formatter);
                    Entry<String> newEvent = new Entry<>(rs.getString(2));
                    newEvent.setId(String.valueOf(rs.getInt(1)));
                    newEvent.setInterval(startDate, endDate);
                    newEvent.setFullDay(true);
                    newEvent.setRecurrenceRule("FREQ=YEARLY");
                    this.holidays.addEntry(newEvent);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // SASANKA - UPDATE - event details were changed
    public void updateEvent(CalendarEvent evt) {
        Entry<String> newEvent = (Entry<String>) evt.getEntry(); // Entry that was changed
        // If event title was changed, update the changes in the DB
        if (evt.getEventType() == CalendarEvent.ENTRY_TITLE_CHANGED) {
            try {
                String sql = "UPDATE events SET title = ? WHERE id = ?";
                PreparedStatement stmt = this.conn.prepareStatement(sql);
                stmt.setString(1, newEvent.getTitle());
                stmt.setString(2, newEvent.getId());
                // Execute SQL Statement
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error Updating Entry Title:");
                System.out.println(e);
            }
            // If event start/end datetime was changed, update the changes in the DB
        } else if (evt.getEventType() == CalendarEvent.ENTRY_INTERVAL_CHANGED) {
            try {
                String sql = "UPDATE events SET start_datetime = ?, end_datetime = ? WHERE id = ?";
                PreparedStatement stmt = this.conn.prepareStatement(sql);
                stmt.setString(1, newEvent.getInterval().getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                stmt.setString(2, newEvent.getInterval().getEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                stmt.setString(3, newEvent.getId());
                // Execute SQL Statement
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error Updating Entry Interval:");
                System.out.println(e);
            }
            // If the event was changed to full day or not, update the changes in the DB
        } else if (evt.getEventType() == CalendarEvent.ENTRY_FULL_DAY_CHANGED) {
            try {
                String sql = "UPDATE events SET full_day = ? WHERE id = ?";
                PreparedStatement stmt = this.conn.prepareStatement(sql);
                stmt.setShort(1, (short) (newEvent.isFullDay() ? 1 : 0));
                stmt.setString(2, newEvent.getId());
                // Execute SQL Statement
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error Updating Entry Full Day:");
                System.out.println(e);
            }
            // If the reccurence rule was changed, update the changes in the DB
        } else if (evt.getEventType() == CalendarEvent.ENTRY_RECURRENCE_RULE_CHANGED) {
            try {
                String sql = "UPDATE events SET recurrence = ? WHERE id = ?";
                PreparedStatement stmt = this.conn.prepareStatement(sql);
                stmt.setString(1, newEvent.getRecurrenceRule());
                stmt.setString(2, newEvent.getId());
                // Execute SQL Statement
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error Updating Entry Recurrence Rule:");
                System.out.println(e);
            }
            // If the event was moved to a different calendar, update the changes in the DB
        } else if (evt.getEventType() == CalendarEvent.ENTRY_CALENDAR_CHANGED && evt.getEntry().getCalendar() != null) {
            System.out.println("Event moved to diff calendar");
            try {
                String sql = "UPDATE events SET calendar = ? WHERE id = ?";
                PreparedStatement stmt = this.conn.prepareStatement(sql);
                switch (newEvent.getCalendar().getName()) {
                    case "Personal":
                        stmt.setString(1, "DEFAULT");
                        break;
                    case "Birthdays":
                        stmt.setString(1, "BIRTHDAYS");
                        break;
                    case "Holidays":
                        stmt.setString(1, "HOLIDAYS");
                        break;
                    default:
                        stmt.setString(1, "DEFAULT");
                        break;
                }
                stmt.setString(2, newEvent.getId());
                // Execute SQL Statement
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error Updating Entry Calendar:");
                System.out.println(e);
            }
        }
    }

    // SASANKA - DELETE - DELETE EVENT FROM DB
    public void deleteEvent(CalendarEvent evt) {
        // If the event has no calendar, it had to be deleted...
        if (evt.getEntry().getCalendar() == null) {
            try {
                System.out.println("Removing event" + evt.getEntry().getId());
                String sql = "DELETE from events  WHERE id = ?";
                PreparedStatement stmt = this.conn.prepareStatement(sql);
                stmt.setString(1, evt.getEntry().getId());
                // Execute SQL Statement
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    public CalendarView getCalView() {
        return this.calendarView;
    }
}
