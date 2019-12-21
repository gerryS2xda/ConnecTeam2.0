package com.example.demo.guess.gamesMenagemet.backend.listeners;

import com.example.demo.entity.Gruppo;
import com.vaadin.flow.component.UI;

public interface BroadcastListener {
    void startGame1(UI ui);
    void receiveIndizio(int indexIndizio, String message);
    void countDown(int time);
    void parolaVotata(Gruppo g);
    void partititaVincente(String parola,int punteggio);
    void partititanonVincente();
    void terminaPartitaForAll(String msgDialog);
    void partitaVincenteForTeacher(Gruppo gruppo);
    void partitaNonVincenteForTeacher(Gruppo gruppo);
}
