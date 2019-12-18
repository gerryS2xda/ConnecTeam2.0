package com.example.demo.users.event;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EndGameEventPublisher implements EndGameEventBeanPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    //method for publish an event
    public void doStuffAndPublishAnEvent(String nameGame, Account account, boolean isEndendForAll, Gruppo gruppo, String statusPartita){
        System.out.println("EndGameEventPublisher: Publish an end game event...");
        EndGameEvent endGameEvent = new EndGameEvent(this, nameGame, account, isEndendForAll, gruppo, statusPartita);
        applicationEventPublisher.publishEvent(endGameEvent);
    }
}
