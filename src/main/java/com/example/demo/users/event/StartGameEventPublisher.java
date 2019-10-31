package com.example.demo.users.event;

import com.example.demo.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class StartGameEventPublisher implements StartGameEventBeanPublisher{

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    //method for publish an event
    public void doStuffAndPublishAnEvent(Map<Account, String> eventContent, boolean statusPartita){
        System.out.println("Publish a custom event...");
        StartGameEvent startGameEventEvent = new StartGameEvent(this, eventContent, statusPartita);
        applicationEventPublisher.publishEvent(startGameEventEvent);
    }
}
