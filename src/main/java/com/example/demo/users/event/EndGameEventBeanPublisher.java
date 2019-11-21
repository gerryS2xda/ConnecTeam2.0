package com.example.demo.users.event;

import com.example.demo.entity.Account;

public interface EndGameEventBeanPublisher {
    void doStuffAndPublishAnEvent(String nameGame, Account account);
}
