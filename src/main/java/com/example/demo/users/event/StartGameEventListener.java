package com.example.demo.users.event;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.example.demo.users.broadcaster.Broadcaster;

import java.util.List;
import java.util.Map;

@Component
public class StartGameEventListener {

    @Async  //esegui in un thread differente
    @EventListener
    public void handleReceiveStarGameEvent(StartGameEvent event){
        System.out.println("StartGameEventListener: receive a event.. ");
        List<Gruppo> dataReceive = event.getGruppiList();
        String nomeGioco = event.getNomeGioco(); //nome del gioco che e' stata avviato

        for(int i = 0; i < dataReceive.size(); i++){
            String game = dataReceive.get(i).getNomeGioco();
            for(Account a : dataReceive.get(i).getMembri()){
                System.out.println("StartGameEventListener.handleReceiveStarGameEvent(): Account: " + a.getNome() + " game:  " + game);
                if (game.equals("Guess")) { //indirizza il giocatore nella pagina di Guess
                    Broadcaster.redirectToGuess(a);
                } else if (game.equals("Maty")) { //indirizza il giocatore nella pagina di Maty
                    Broadcaster.redirectToMaty(a);
                }
            }
        }

        Broadcaster.setGruppiListReceive(dataReceive);

        //Avvia il gioco in background per il teacher (necessario perche' non viene memorizzato stato attuale della partita)
        if(nomeGioco.equals("Guess")) {
            Broadcaster.startGameTeacherInBackground("Guess");
        }else if(nomeGioco.equals("Maty")){
            Broadcaster.startGameTeacherInBackground("Maty");
        }

    }
}
