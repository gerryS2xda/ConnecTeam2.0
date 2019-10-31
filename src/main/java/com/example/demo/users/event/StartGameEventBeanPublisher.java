package com.example.demo.users.event;

import com.example.demo.entity.Account;

import java.util.Map;

public interface StartGameEventBeanPublisher {
    void doStuffAndPublishAnEvent(Map<Account, String> eventContent, boolean statusPartita);
}
