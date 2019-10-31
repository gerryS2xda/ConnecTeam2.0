package com.example.demo.users.event;

import com.example.demo.entity.Account;

import java.util.Map;

public interface AccountListEventBeanPublisher {
    void doStuffAndPublishAnEvent(Map<Account, String> eventContent);
}
