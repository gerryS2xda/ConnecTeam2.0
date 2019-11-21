package com.example.demo.users.event;

import org.springframework.context.ApplicationEvent;

public class EndGameEvent extends ApplicationEvent {
    
    private String nameGame;    //nome del gioco che e' terminato

    public EndGameEvent(Object source, String nameGame){
        super(source);
        this.nameGame = nameGame;
    }

    public String getEndNameGame(){
        return nameGame;
    }

}
