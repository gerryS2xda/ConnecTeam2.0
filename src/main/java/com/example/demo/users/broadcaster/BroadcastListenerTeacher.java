package com.example.demo.users.broadcaster;

import com.example.demo.entity.Account;

public interface BroadcastListenerTeacher {

    void updateAndMergeAccountList();
    void startGameInBackground(String game);
    void removeAccountFromAllGrid(Account a);
    void removeAccountFromThisGrid(Account a, String gridName);
}
