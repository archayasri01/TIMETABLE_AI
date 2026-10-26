package com.example.demo.exception;

public class TimetableConflictException extends RuntimeException {

    public TimetableConflictException(String message) {
        super(message);
    }
}