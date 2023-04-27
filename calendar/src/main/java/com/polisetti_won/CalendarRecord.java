/*
    Sasanka Polisetti
    AP CSA
    Cmdr. Schenk
    2nd Period
    27 April 2023
    Master Project - POCO Class modeling a Calendar Record
*/

package com.polisetti_won;

import com.calendarfx.model.Entry;

import java.time.LocalDateTime;

public class CalendarRecord {
    //Fields
    private int id;
    private String title;
    private String calendar;
    private LocalDateTime start;
    private LocalDateTime end;
    private String recurrence;
    private boolean fullDay;
    private String location;



    //Partial Constructor
    public CalendarRecord(String title, String calendar, LocalDateTime start, LocalDateTime end, String recurrence, boolean fullDay, String location) {
        this.id = -1;
        this.title = title;
        this.calendar = calendar;
        this.start = start;
        this.end = end;
        this.recurrence = recurrence;
        this.fullDay = fullDay;
        this.location = location;
    }


    // Full constructor
    public CalendarRecord(int id, String title, String calendar, LocalDateTime start, LocalDateTime end, String recurrence, boolean fullDay, String location) {
        //Chaining partial constructor
        this(title, calendar, start, end, recurrence, fullDay, location);
        this.id = id;
     }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }
    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }
    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
    public String getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }
    public boolean isFullDay() {
        return this.fullDay;
    }

    public void setFullDay(boolean fullDay) {
        this.fullDay = fullDay;
    }

   public String getLocation() {return this.location;}

    public void setLocation(String location) {this.location = location;}

    public Entry<String> createEntry() {

        Entry<String> newEvent = new Entry<>(calendar);
        newEvent.setId(Integer.valueOf(id).toString());
        newEvent.setTitle(title);
        newEvent.setInterval(start, end);
        newEvent.setRecurrenceRule(recurrence);
        newEvent.setFullDay(fullDay);
        newEvent.setLocation(location);

        return newEvent;


    }
}
