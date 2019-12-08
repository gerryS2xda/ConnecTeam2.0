package com.example.demo.users.broadcaster;

import com.example.demo.entity.Account;

public interface BroadcastListenerTeacher {

    void updateGridStudentCollegati();
    void startGameInBackground(String game);
    void removeAccountFromAllGrid(Account a);
    void removeAccountFromThisGrid(Account a, String gridName);
    void showDialogFinePartitaTeacher(String nameGame);
}
