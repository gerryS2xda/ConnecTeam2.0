package com.example.demo.users.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EndGameEventListener {

    @Async  //esegui in un thread differente
    @EventListener
    public void handleReceiveEndGameEvent(EndGameEvent event){
        System.out.println("EndGameEventListener: receive a event");
    }
}
