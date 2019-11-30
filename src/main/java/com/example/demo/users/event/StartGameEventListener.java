package com.example.demo.users.event;

import com.example.demo.entity.Account;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.example.demo.users.broadcaster.Broadcaster;
import java.util.Map;

@Component
public class StartGameEventListener {

    @Async  //esegui in un thread differente
    @EventListener
    public void handleReceiveStarGameEvent(StartGameEvent event){
        System.out.println("StartGameEventListener: receive a event.. ");
        Map<Account, String> dataReceive = event.getAccountList();

        for(Account i : dataReceive.keySet()){ //per tutti gli account ottenuti dall'event e quindi assegnati dal teacher
            String game = dataReceive.get(i); //dammi il nome del gioco associato all'account
            System.out.println("StartGameEventListener.handleReceiveStarGameEvent(): Account: " + i.getNome() + " game:  " + game);
            if (game.equals("Guess")) { //indirizza il giocatore nella pagina di Guess
                Broadcaster.redirectToGuess(i);
            } else if (game.equals("Maty")) { //indirizza il giocatore nella pagina di Maty
                Broadcaster.redirectToMaty(i);
            }
        }

        //Avvia il gioco in background per il teacher (necessario perche' non viene memorizzato stato attuale della partita)
        if(dataReceive.containsValue("Guess")) {
            Broadcaster.startGameTeacherInBackground("Guess");
        }else if(dataReceive.containsValue("Maty")){
            Broadcaster.startGameTeacherInBackground("Maty");
        }
    }
}
