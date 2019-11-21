package com.example.demo.users.event;

import com.example.demo.entity.Account;
import com.example.demo.users.broadcaster.Broadcaster;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EndGameEventListener {

    @Async  //esegui in un thread differente
    @EventListener
    public void handleReceiveEndGameEvent(EndGameEvent event){
        System.out.println("EndGameEventListener: receive a event");
        String nameGame = event.getNameOfGameEnded();
        Account acc = event.getAccount(); //usa account per impostare cosa deve student e teacher
        if(nameGame.equals("Guess")){
            Broadcaster.setIsGuessStart(false);
        }else if(nameGame.equals("Maty")){
            Broadcaster.setIsMatyStart(false);
        }
    }
}
