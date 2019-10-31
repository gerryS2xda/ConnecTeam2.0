package com.example.demo.users.event;

import com.example.demo.entity.Account;
import com.example.demo.users.broadcaster.Broadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountListEventPublisher implements AccountListEventBeanPublisher{

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    //method for publish an event
    public void doStuffAndPublishAnEvent(Map<Account, String> eventContent){
        System.out.println("Publish a custom event...");
        AccountListEvent accountListEvent = new AccountListEvent(this, eventContent);
        applicationEventPublisher.publishEvent(accountListEvent);
    }
}
