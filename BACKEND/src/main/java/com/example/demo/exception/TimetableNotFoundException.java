package com.example.demo.exception;

public class TimetableNotFoundException extends RuntimeException {
    public TimetableNotFoundException(String message) {
        super(message);
    }

    public TimetableNotFoundException() {
        super("Timetable not found");
    }
}
