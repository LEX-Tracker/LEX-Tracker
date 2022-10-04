package com.digitalinterruption.lex.calender;

import java.time.LocalDateTime;

public class EventObjects {
    private int id;
    private String message;
    private LocalDateTime date;
    private int color;
    public EventObjects(String message, LocalDateTime date) {
        this.message = message;
        this.date = date;
    }
    public EventObjects(int id, String message, LocalDateTime date) {
        this.date = date;
        this.message = message;
        this.id = id;
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
        return color;
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