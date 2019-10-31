package com.example.demo.gamesRules;

public class DuplicateGameNameException extends RuntimeException {
    public DuplicateGameNameException(String message) {
        super(message);
    }
}
