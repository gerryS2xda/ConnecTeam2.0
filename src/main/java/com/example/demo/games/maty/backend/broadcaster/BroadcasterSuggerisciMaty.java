package com.example.demo.games.maty.backend.broadcaster;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.games.maty.backend.db.ItemMaty;
import com.example.demo.games.maty.backend.listeners.SuggerisciListenerMaty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BroadcasterSuggerisciMaty implements Serializable {

    private static final Map<Account, SuggerisciListenerMaty> listeners = new HashMap<>();
    private static final ArrayList<ItemMaty> items = new ArrayList<>();

    public static synchronized void addItems(ItemMaty itemMaty){
        items.add(itemMaty);
    }

    public static ArrayList<ItemMaty> getItems() {
        return items;
    }

    public static synchronized void register(Account account, SuggerisciListenerMaty listener) {
        listeners.put(account, listener);
    }

    public static synchronized void broadcast(final String message, String operazione, boolean operation, Account acc, Gruppo g) {
        listeners.forEach((account, listenerMaty) -> {
            listenerMaty.operazione(message, operazione, operation, acc, g);
        });

    }

    public static synchronized void refreshContent(){
        listeners.forEach(((account, listenerMaty) -> {
            listenerMaty.refreshContent();
        }));
    }

    public static synchronized void reset(){
        listeners.forEach(((account, listenerMaty) -> {
            listenerMaty.reset();
        }));
    }

    //getter and setter
    public static Map<Account, SuggerisciListenerMaty> getListeners() {
        return listeners;
    }

}
