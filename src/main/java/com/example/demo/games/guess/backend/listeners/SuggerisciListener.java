package com.example.demo.games.guess.backend.listeners;

import com.example.demo.entity.Gruppo;

public interface SuggerisciListener {
    void receiveBroadcast(Gruppo gruppo, String message);
}
