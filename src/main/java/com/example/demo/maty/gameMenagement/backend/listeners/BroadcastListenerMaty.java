package com.example.demo.maty.gameMenagement.backend.listeners;

import com.example.demo.entity.Gruppo;

public interface BroadcastListenerMaty{
    void startGame1();
    void receiveIndizio(String message);
    void countDown(int time);
    void partitaVincente(String parola,int punteggio);
    void partitaNonVincente();
    void numeroDaSotrarre(String numero,String numOriginale);
    void numeroDaSommare(String numOriginaale);
    void partitaVincenteForTeacher(Gruppo gruppo);
    void partitaNonVincenteForTeacher(Gruppo gruppo);
    void terminaPartitaForAll(String msgDialog);
}
