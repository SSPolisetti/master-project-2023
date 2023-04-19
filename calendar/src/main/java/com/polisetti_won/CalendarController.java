package com.polisetti_won;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.event.EventHandler;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CalendarController {

    // Fields
    public CalendarView calendarView;
    public Calendar personal;
    public Calendar birthdays;
    public Calendar holidays;
    private Connection conn;
    private ResultSet rs;

    // Contructor
    public CalendarController() {
        // Init Calendar GUI
        this.calendarView = new CalendarView();
        // Add Extra Calendars
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
        this.personal.addEventHandler(onAdd);
        this.personal.addEventHandler(onChange);
        this.birthdays.addEventHandler(onAdd);
        this.birthdays.addEventHandler(onChange);
        this.holidays.addEventHandler(onAdd);
        this.holidays.addEventHandler(onChange);

    }

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

    // CREATE - event was created
    public void addEvent(CalendarEvent evt) {
        System.out.println(evt.getEventType());
        if (evt.getEventType() == CalendarEvent.ENTRY_CALENDAR_CHANGED && evt.getEntry().getId().length() > 8) {
            System.out.println("Entry created");
            Entry<String> newEvent = (Entry<String>) evt.getEntry();
            System.out.println(newEvent.getTitle());
            System.out.println();
            // Add to DB
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

    // READ - Load events from DB into calendar
    public void loadEvents() {
        // Load Events from DB
        try {
            Statement stmt = this.conn.createStatement();
            rs = stmt.executeQuery("SELECT * from events");
            while(rs.next()) {
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
                } else if (Objects.equals(rs.getString(3), "BIRTHDAYS")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime startDate = LocalDateTime.parse(rs.getString(4), formatter);
                    LocalDateTime endDate = LocalDateTime.parse(rs.getString(5), formatter);
                    Entry<String> newEvent = new Entry<>(rs.getString(2));
                    newEvent.setId(String.valueOf(rs.getInt(1)));
                    newEvent.setInterval(startDate, endDate);
                    newEvent.setFullDay((rs.getShort(7) == 1));
                    newEvent.setRecurrenceRule(rs.getString(6));
                    this.birthdays.addEntry(newEvent);
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

    // UPDATE - event details were changed
    public void updateEvent(CalendarEvent evt) {
        System.out.println(evt.getEventType());
        System.out.println(evt.getEntry().getId());
        Entry<String> newEvent = (Entry<String>) evt.getEntry();
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
        } else if (evt.getEventType() == CalendarEvent.ENTRY_CALENDAR_CHANGED) {
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

    public CalendarView getCalView() {
        return this.calendarView;
    }
}
