package com.example.demo.users.event;

import com.example.demo.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AccountListEventPublisher implements AccountListEventBeanPublisher{

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    //method for publish an event
    public void doStuffAndPublishAnEvent(List<Account> eventContent, String operationName){
        System.out.println("AccountListEventPublisher: Publish a custom event...");
        AccountListEvent accountListEvent = new AccountListEvent(this, eventContent, operationName);
        applicationEventPublisher.publishEvent(accountListEvent);
    }
}
