package com.example.demo.users.event;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;

public interface EndGameEventBeanPublisher {
    void doStuffAndPublishAnEvent(String nameGame, Account account, boolean isEndendForAll, Gruppo gruppo, String statusPartita);
}
