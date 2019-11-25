package com.example.demo.users.broadcaster;

public interface BroadcastListenerTeacher {

    void updateAndMergeAccountList();
    void startGameInBackground(String game);
    void removeAccountFromAllGrid();
}
