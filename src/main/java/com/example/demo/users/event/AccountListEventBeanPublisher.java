package com.example.demo.users.event;

import com.example.demo.entity.Account;
import java.util.List;

public interface AccountListEventBeanPublisher {
    void doStuffAndPublishAnEvent(List<Account> eventContent, String operationName);
}
