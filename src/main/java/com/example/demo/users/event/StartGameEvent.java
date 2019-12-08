package com.example.demo.users.event;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Map;

public class StartGameEvent extends ApplicationEvent {

    //instance field
    private List<Gruppo> gruppiList;  //mappa il nome di un gioco ad una lista di gruppi
    private String nomeGioco; //nome del gioco che e' stato avviato

    public StartGameEvent(Object source, List<Gruppo> gruppi, String nomeGioco){
        super(source);
        gruppiList = gruppi;
        this.nomeGioco = nomeGioco;
    }

    public List<Gruppo> getGruppiList(){
        return gruppiList;
    }

    public String getNomeGioco(){
        return nomeGioco;
    }
}
