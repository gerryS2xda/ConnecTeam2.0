package com.example.demo.users.event;

import com.example.demo.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EndGameEventPublisher implements EndGameEventBeanPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    //method for publish an event
    public void doStuffAndPublishAnEvent(String nameGame, Account account){
        System.out.println("Publish an end game event...");
        EndGameEvent endGameEvent = new EndGameEvent(this, nameGame, account);
        applicationEventPublisher.publishEvent(endGameEvent);
    }
}
