package com.digitalinterruption.lex.calender;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventObject {
    private int id;
    private String message;
    private LocalDateTime date;
    private int color;
    public EventObject(String message, LocalDate date) {
        this.message = message;
        this.date = date.atStartOfDay();
    }
    public EventObject(int id, String message, LocalDate date) {
        this.date = date.atStartOfDay();
        this.message = message;
        this.id = id;
    }

    public EventObject(int id, String message, LocalDate date, int color){
        this.date = date.atStartOfDay();
        this.message = message;
        this.id = id;
        this.color = color;
    }
    public int getId() {
        return id;
    }
    public String getMessage() {
        return message;
    }
    public LocalDateTime getDate() {
        return date;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}