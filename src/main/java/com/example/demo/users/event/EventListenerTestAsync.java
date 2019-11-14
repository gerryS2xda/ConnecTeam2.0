package com.example.demo.users.event;

import com.example.demo.entity.Account;
import com.vaadin.flow.component.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class EventListenerTestAsync {

    @Autowired
    private StartGameEventListener startGameEventListener;

    //instance field per gestire comunicazione tra teacher e discusser
    private Map<Integer, Consumer<UI>> gamesLaunch = new HashMap<>(); //mappa una gioco da lanciare con un intero
    private Map<Account, UI> uiAccountList = new HashMap<>(); //mappa una ui ad un account

    //Comunicazione tra teacher e discusser per avviare i giochi
    @Async
    public void registerEventListenerForDiscusser(UI ui, Account a, Consumer<UI> guess, Consumer<UI> maty){
        gamesLaunch.put(1, guess);
        gamesLaunch.put(2, maty);
        uiAccountList.put(a, ui);
    }

    @Async
    @EventListener
    public void handleReceiveStarGameEvent(StartGameEvent event){
        System.out.println("Receive a custom event: " + event.getAccountList());
        //NOTA: Questo funziona solo se il teacher avvia tutte e tre le partite contemporaneamente
        // se la partita e' stata avviata dal teacher
        Map<Account, String> dataReceive = event.getAccountList();

        for(Account i : dataReceive.keySet()){ //per tutti gli account ottenuti dall'event e quindi assegnati dal teacher

            UI ui = uiAccountList.get(i); //dammi la UI associata all'account i
            System.out.println("handleReceiveStarGameEvent: ui " + ui);
                String game = dataReceive.get(i); //dammi il nome del gioco associato all'account
                System.out.println("handleReceiveStarGameEvent: game  " + game);
                if (game.equals("Guess")) { //indirizza il giocatore nella pagina di Guess
                    gamesLaunch.get(1).accept(ui);
                } else if (game.equals("Maty")) { //indirizza il giocatore nella pagina di Maty
                    gamesLaunch.get(2).accept(ui);
                }

        }
    }
}
