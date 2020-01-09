package com.example.demo.games.guess.backend.listeners;

import com.example.demo.entity.Gruppo;
import com.vaadin.flow.component.UI;

public interface BroadcastListener {
    void startGame1(UI ui);
    void receiveIndizio(int indexIndizio, String message);
    void countDown(int time);
    void parolaVotata(Gruppo g);
    void partitaVincente(String parola,int punteggio);
    void partitaNonVincente();
    void terminaPartitaForAll(String msgDialog);
    void partitaVincenteForTeacher(Gruppo gruppo);
    void partitaNonVincenteForTeacher(Gruppo gruppo);
}
