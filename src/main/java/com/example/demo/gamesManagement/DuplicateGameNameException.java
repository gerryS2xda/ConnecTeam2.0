package com.example.demo.gamesManagement;

public class DuplicateGameNameException extends RuntimeException {
    public DuplicateGameNameException(String message) {
        super(message);
    }
}
