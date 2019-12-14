package com.example.demo.guess.gamesMenagemet.backend.listeners;

import com.example.demo.entity.Gruppo;

public interface ChatListener {
    void receiveBroadcast(Gruppo g, String message); //g e' il gruppo da cui e' stato inviato il messaggio
}
