package com.example.demo.users.broadcaster;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;

public interface BroadcastListenerTeacher {

    void updateGridStudentCollegati();
    void startGameInBackground(String game);
    void removeAccountFromAllGrid(Account a);
    void configFinePartitaTeacher(String nameGame, Gruppo g, String statusPartita);
}
