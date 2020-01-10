package com.example.demo.games.nuovoGioco;

import com.example.demo.gamesManagement.Game;
import org.springframework.stereotype.Component;

@Component
public class NuovoGioco implements Game {

    @Override
    public String getNomeGioco() {
        return "NuovoGioco";
    }

    @Override
    public String getDescrizioneGioco() {
        return "Descrizione breve del gioco";
    }

    @Override
    public String getDescrizioneLungaGioco() {
        return "Descrizione lunga del gioco";
    }

    @Override
    public String getPathName() {
        return "Inserire il route della pagina del gioco";
    }

    @Override
    public String getAgeRange() {
        return "Range di età del gioco";
    }
}
