package com.example.demo.guess.gamesMenagemet.backend.listeners;

import com.example.demo.entity.Gruppo;

public interface SuggerisciListener {
    void receiveBroadcast(Gruppo gruppo, String message);
}
