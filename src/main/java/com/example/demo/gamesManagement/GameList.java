package com.example.demo.gamesManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
public class GameList {

    @Autowired
    private List<Game> list;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) throws DuplicateGameNameException {
        System.out.println("GameList: I giochi sono " + list.size());
        HashSet<String> hashSet = new HashSet<>();
        for(Game i : list) {
            if(!hashSet.add(i.getNomeGioco().toLowerCase()))
                throw  new DuplicateGameNameException("nome duplicato: "+ i.getNomeGioco());
        }
        list.forEach(controller -> {
            System.out.println("GameList: " + controller.getClass().getName());
        });

    }

    public List<Game> getGameList(){
        return list;
    }

    public List<String> getGameNames(){
        ArrayList<String> gameNames=new ArrayList<>();
        for (Game g: list) {
            gameNames.add(g.getNomeGioco());
        }
        gameNames.sort(String::compareTo);
        return  gameNames;
    }

}
