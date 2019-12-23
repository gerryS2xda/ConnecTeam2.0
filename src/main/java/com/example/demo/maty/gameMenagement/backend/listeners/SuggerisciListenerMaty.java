package com.example.demo.maty.gameMenagement.backend.listeners;

import com.example.demo.entity.Gruppo;

public interface SuggerisciListenerMaty {
    void operazione(String message, String operazione, boolean operation, Gruppo g);
}
