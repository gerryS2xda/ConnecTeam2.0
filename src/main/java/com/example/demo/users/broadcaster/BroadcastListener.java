package com.example.demo.users.broadcaster;

import com.example.demo.entity.Account;
import com.vaadin.flow.component.UI;

public interface BroadcastListener {

    void redirectToGuess();
    void redirectToMaty();
    void updateAndMergeAccountList();
    void startGameInBackground(String game);
}
