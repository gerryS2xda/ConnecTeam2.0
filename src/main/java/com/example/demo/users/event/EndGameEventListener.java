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
        System.out.println("EndGameEventListener: receive a event; Account: " + event.getAccount().getNome());
        String nameGame = event.getNameOfGameEnded();
        Account acc = event.getAccount();
        if(event.isEndendForAll()) {
            if (nameGame.equals("Guess")) {
                Broadcaster.setIsGuessStart(false);
            } else if (nameGame.equals("Maty")) {
                Broadcaster.setIsMatyStart(false);
            }
        }
        if(acc.getTypeAccount().equals("student")){
            Broadcaster.removeAccountFromThisGrid(acc, nameGame); //assumiamo che nameGame sia uguale a quello della grid in GestStudentUI
        }else if(acc.getTypeAccount().equals("teacher")){
            Broadcaster.showDialogFinePartitaTeacher(nameGame);
        }

    }
}
