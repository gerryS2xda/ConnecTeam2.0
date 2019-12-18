package com.example.demo.users.event;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
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
        Gruppo g = event.getGruppo(); //gruppo che ha terminato la partita
        String statusPartita = event.getStatusPartita();

        if(event.isEndendForAll()) {
            if (nameGame.equals("Guess")) {
                Broadcaster.setIsGuessStart(false);
            } else if (nameGame.equals("Maty")) {
                Broadcaster.setIsMatyStart(false);
            }
            Broadcaster.showDialogFinePartitaTeacher(nameGame, g, "");
        }

        if(acc.getTypeAccount().equals("student")){
            Broadcaster.removeAccountFromAllGrid(acc);
        }else if(statusPartita.equals("vincente")){
            Broadcaster.showDialogFinePartitaTeacher(nameGame, g, statusPartita);
        }else if(statusPartita.equals("non-vincente")){
            Broadcaster.showDialogFinePartitaTeacher(nameGame, g, statusPartita);
        }

    }
}
