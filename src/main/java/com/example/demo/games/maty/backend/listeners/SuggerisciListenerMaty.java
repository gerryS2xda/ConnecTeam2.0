package com.example.demo.games.maty.backend.listeners;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;

public interface SuggerisciListenerMaty {
    void operazione(String message, String operazione, boolean operation, Account acc, Gruppo g);
    void refreshContent();
    void reset();
}
