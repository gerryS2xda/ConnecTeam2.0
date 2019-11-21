package com.example.demo.users.event;

import com.example.demo.entity.Account;
import org.springframework.context.ApplicationEvent;

public class EndGameEvent extends ApplicationEvent {

    private String nameGame;    //nome del gioco che e' terminato
    private Account account; //account per distinguere cosa deve fare eventlistener tra student e teacher

    public EndGameEvent(Object source, String nameGame, Account account){
        super(source);
        this.nameGame = nameGame;
        this.account = account;
    }

    public String getNameOfGameEnded(){
        return nameGame;
    }

    public Account getAccount(){
        return account;
    }

}
