/*
    Sasanka Polisetti
    AP CSA
    Cmdr. Schenk
    2nd Period
    27 April 2023
    Master Project - POCO Class modeling a Calendar Record
*/

package com.polisetti_won;

import java.time.LocalDateTime;

public class CalendarRecord {
    //Fields
    private int id;
    private String title;
    private String calendar;
    private LocalDateTime start;
    private LocalDateTime end;
    private String recurrence;
    private boolean fullday;

    // Default contructor
    public CalendarRecord(int id, String title, String calendar, LocalDateTime start, LocalDateTime end, String recurrence, boolean fullday) {
        this.id = id;
        this.title = title;
        this.calendar = calendar;
        this.start = start;
        this.end = end;
        this.recurrence = recurrence;
        this.fullday = fullday;
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
    public boolean isFullday() {
        return fullday;
    }
    public void setFullday(boolean fullday) {
        this.fullday = fullday;
    }
}
