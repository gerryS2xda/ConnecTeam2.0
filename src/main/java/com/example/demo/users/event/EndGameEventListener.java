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
                Broadcaster.setCountGuessUser(0);
            } else if (nameGame.equals("Maty")) {
                Broadcaster.setIsMatyStart(false);
                Broadcaster.setCountMatyUser(0);
            }
            Broadcaster.configFinePartitaTeacher(nameGame, g, "");
        }

        if(acc.getTypeAccount().equals("student")){
            Broadcaster.removeAccountFromAllGrid(acc);
            Broadcaster.removeItemFromAccountList(acc);
        }

        //NOTA: occorre sistemare partita vincente in modo tale che non termina per tutti se uno dei gruppi vince
        //NOTA: Per il teacher occorre resettare il tutto altrimenti mostra la schermata del gioco bloccata,
        //in quanto la partita e' terminata per tutti, anche se ha vinto un solo gruppo
        if(statusPartita.equals("vincente")){
            Broadcaster.configFinePartitaTeacher(nameGame, g, statusPartita);
            Broadcaster.setIsGuessStart(false);
            Broadcaster.setCountGuessUser(0);
            Broadcaster.setIsMatyStart(false);
            Broadcaster.setCountMatyUser(0);
        }else if(statusPartita.equals("non-vincente")){
            Broadcaster.configFinePartitaTeacher(nameGame, g, statusPartita);
            Broadcaster.setIsGuessStart(false);
            Broadcaster.setCountGuessUser(0);
            Broadcaster.setIsMatyStart(false);
            Broadcaster.setCountMatyUser(0);
        }

    }
}
