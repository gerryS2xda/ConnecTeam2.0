package com.example.demo.users.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EndGameEventPublisher implements EndGameEventBeanPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    //method for publish an event
    public void doStuffAndPublishAnEvent(String nameGame){
        System.out.println("Publish an end game event...");
        EndGameEvent endGameEvent = new EndGameEvent(this, nameGame);
        applicationEventPublisher.publishEvent(endGameEvent);
    }
}
