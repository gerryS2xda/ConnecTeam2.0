package com.example.demo.maty.gameMenagement.backend.broadcaster;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.maty.gameMenagement.backend.db.ItemMaty;
import com.example.demo.maty.gameMenagement.backend.listeners.SuggerisciListenerMaty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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

    public static void broadcast(final String message, String operazione, boolean operation, Gruppo g) {
        listeners.forEach((account, listenerMaty) -> {
            listenerMaty.operazione(message, operazione, operation, g);
        });

    }

    public static Map<Account, SuggerisciListenerMaty> getListeners() {
        return listeners;
    }

}
