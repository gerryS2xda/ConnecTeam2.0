package com.example.demo.guess.gamesMenagemet.backend.broadcaster;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.guess.gamesMenagemet.backend.listeners.SuggerisciListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BroadcasterSuggerisci implements Serializable {

    private static final Map<Account, SuggerisciListener> listeners = new HashMap<>();

    public static synchronized void register(Account account, SuggerisciListener listener) {
        listeners.put(account, listener);
    }

    public static void broadcast(Gruppo g, final String message) {

        listeners.forEach((account, broadcastListener) -> {
            broadcastListener.receiveBroadcast(g, message);
        });
    }

    public static Map<Account, SuggerisciListener> getListeners() {
        return listeners;
    }
}
