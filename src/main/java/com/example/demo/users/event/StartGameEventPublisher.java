package com.example.demo.users.event;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StartGameEventPublisher implements StartGameEventBeanPublisher{

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    //method for publish an event
    public void doStuffAndPublishAnEvent(List<Gruppo> eventContent, String nomeGioco){
        System.out.println("StartGameEventPublisher: Publish a custom event...");
        StartGameEvent startGameEventEvent = new StartGameEvent(this, eventContent, nomeGioco);
        applicationEventPublisher.publishEvent(startGameEventEvent);
    }
}
