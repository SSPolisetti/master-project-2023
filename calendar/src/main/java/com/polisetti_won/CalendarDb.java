/*
    Tillman Won, Sasanka Polisetti
    AP CSA
    Cmdr. Schenk
    2nd Period
    27 April 2023
    Master Project - POCO Class modeling a Calendar Record
*/
package com.polisetti_won;

import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.Entry;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CalendarDb {

    // Fields
    private ArrayList<CalendarRecord> results = new ArrayList<>();
    private static Connection conn;
    private ResultSet rs;
    private boolean connected;


    //Tillman and Sasanka - Constructor
    public CalendarDb() {
        super();
        this.connected = false;

    }

    //Getters and Setters

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public ArrayList<CalendarRecord> getResults() {
        return results;
    }

    public void setResults(ArrayList<CalendarRecord> results) {
        this.results = results;
    }

    public static Connection getConn() {
        return conn;
    }

    public static void setConn(Connection conn) {
        CalendarDb.conn = conn;
    }

    public ResultSet getRs() {
        return rs;
    }

    public void setRs(ResultSet rs) {
        this.rs = rs;
    }

    // Tillman - Connect to MySQL database
    public void connect() {
        if (!isConnected()) {
            // Try catch for attempting connection
            try {
                conn = DriverManager.getConnection("jdbc:mysql://192.168.0.170:3306/sasanka_tillman", "lightning", "jchs"); //jdbc:mysql://127.0.0.1:3306/calendar, "root", "SQL_sp_532"
                setConnected(true);
                loadEvents();

            } catch(Exception e) {
                System.out.println("Error Connecting to Database: \n");
                e.printStackTrace();
            }
        }


    }

    // Sasanka - Disconnect method
    public void disconnect() {
        if (isConnected()) {
            // Try catch for attempting connection
            try {

                conn.close();
                setConnected(false);

            } catch (Exception e) {
                System.out.println("Error disconnecting to Database: \n");
                e.printStackTrace();
            }
        }

    }

    // *********************
    // CREATE, READ, UPDATE, DELETE

    // Tillman - CREATE - event was created
    public void addEvent(Entry<String> newEvent, CalendarRecord record) {

        try {

            // Prepare SQL Statement
            String query = "INSERT INTO events (title, calendar, start_datetime, end_datetime, recurrence, full_day, location) VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(query);

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

            stmt.setString(7, newEvent.getLocation());

            // Execute SQL Statement
            stmt.executeUpdate();

            // Get ID of new entry to have matching calendar entry and db record IDs
            rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");

            rs.next();
            int id = rs.getInt(1);
            newEvent.setId(String.valueOf(id));

            this.results.add(new CalendarRecord(id, record.getTitle(), record.getCalendar(), record.getStart(), record.getEnd(), record.getRecurrence(), record.isFullDay(), record.getLocation()));



        } catch (Exception e) {
            System.out.println("Error Creating Entry:\n");
            e.printStackTrace();
        }
    }


    // Tillman - READ - Load events from DB into calendar
    public void loadEvents() {
        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM events");

            //DateTimeFormatter to format SQL Datetime to Java Datetime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            //Looping through records to construct CalendarRecord objects for container
            while(rs.next()) {

                LocalDateTime startDate = LocalDateTime.parse(rs.getString(4), formatter);
                LocalDateTime endDate = LocalDateTime.parse(rs.getString(5), formatter);

                String calendar = "";

                switch (rs.getString(3)) {
                    case "DEFAULT":
                        calendar = "Personal";
                        break;
                    case "BIRTHDAYS":
                        calendar = "Birthdays";
                        break;
                    case "HOLIDAYS":
                        calendar = "Holidays";
                        break;
                    default:
                        calendar = "Personal";
                }

                CalendarRecord record = new CalendarRecord(rs.getInt(1), rs.getString(2), calendar, startDate, endDate,rs.getString(6),(rs.getShort(7) == 1), rs.getString(8));


                results.add(record);

            }

        } catch (Exception e) {
            System.out.println("Error Loading Entries:\n");
            e.printStackTrace();
        }
    }


    // Sasanka - UPDATE - event details were changed
    public void updateEvent(CalendarEvent evt, Entry<String> newEvent) {


        try {

            if (evt.getEventType() == CalendarEvent.ENTRY_TITLE_CHANGED) {
                String query = "UPDATE events SET title = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setString(1, newEvent.getTitle());

                stmt.setString(2, newEvent.getId());

                stmt.executeUpdate();


            } else if (evt.getEventType() == CalendarEvent.ENTRY_INTERVAL_CHANGED) {

                String sql = "UPDATE events SET start_datetime = ?, end_datetime = ? WHERE id = ?";
                PreparedStatement stmt = this.conn.prepareStatement(sql);
                stmt.setString(1, newEvent.getInterval().getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                stmt.setString(2, newEvent.getInterval().getEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                stmt.setString(3, newEvent.getId());
                // Execute SQL Statement
                stmt.executeUpdate();

            } else if (evt.getEventType() == CalendarEvent.ENTRY_FULL_DAY_CHANGED) {
                String sql = "UPDATE events SET full_day = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setShort(1, (short) (newEvent.isFullDay() ? 1 : 0));
                stmt.setString(2, newEvent.getId());
                // Execute SQL Statement
                stmt.executeUpdate();
            } else if (evt.getEventType() == CalendarEvent.ENTRY_RECURRENCE_RULE_CHANGED) {

                String sql = "UPDATE events SET recurrence = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newEvent.getRecurrenceRule());
                stmt.setString(2, newEvent.getId());
                // Execute SQL Statement
                stmt.executeUpdate();
            } else if (evt.getEventType() == CalendarEvent.ENTRY_CALENDAR_CHANGED && evt.getEntry().getCalendar() != null) {

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

            } else if (evt.getEventType() == CalendarEvent.ENTRY_LOCATION_CHANGED) {
                String sql = "UPDATE events SET location = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newEvent.getLocation());
                stmt.setString(2, newEvent.getId());
                // Execute SQL Statement
                stmt.executeUpdate();
            }

        } catch (Exception e) {
            System.out.println("Error Updating Entry:\n");
            e.printStackTrace();
        }
    }

    // Sasanka - DELETE - event was deleted
    public void deleteEvent(Integer id) {
        try {
            System.out.println("Removing event: " + id.toString());
            String sql = "DELETE from events  WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            // Execute SQL Statement
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error Deleting Entry:\n");
            e.printStackTrace();
        }
    }


}
