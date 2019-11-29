package com.example.demo.guess.gamesMenagemet.backend.listeners;

import com.example.demo.entity.Account;
import com.vaadin.flow.component.UI;

public interface BroadcastListener {
    void countUser(UI ui, String nome);
    void startGame1(UI ui);
    void receiveIndizio(String message);
    void countDown(String n);
    void addUsers(UI ui,int i);
    void parolaVotata();
    void partititaVincente(String parola,int punteggio);
    void partititanonVincente();
    void terminaPartitaFromTeacher();
}
