package com.example.demo.users.event;

import com.example.demo.entity.Account;
import org.springframework.context.ApplicationEvent;

public class EndGameEvent extends ApplicationEvent {

    private String nameGame;    //nome del gioco che e' terminato
    private Account account; //account per distinguere cosa deve fare eventlistener tra student e teacher
    private boolean isEndendForAll; //il gioco e' terminato per tutti gli utenti che erano connessi?

    public EndGameEvent(Object source, String nameGame, Account account, boolean isEndendForAll){
        super(source);
        this.nameGame = nameGame;
        this.account = account;
        this.isEndendForAll = isEndendForAll;
    }

    public String getNameOfGameEnded(){
        return nameGame;
    }

    public Account getAccount(){
        return account;
    }

    public boolean isEndendForAll(){
        return isEndendForAll;
    }
}
