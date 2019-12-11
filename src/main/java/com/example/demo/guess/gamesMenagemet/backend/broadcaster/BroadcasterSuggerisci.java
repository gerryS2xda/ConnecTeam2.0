package com.example.demo.guess.gamesMenagemet.backend.broadcaster;

import com.example.demo.entity.Gruppo;
import com.example.demo.guess.gamesMenagemet.backend.listeners.SuggerisciListener;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BroadcasterSuggerisci implements Serializable {

    private static final List<SuggerisciListener> listeners = new CopyOnWriteArrayList<SuggerisciListener>();

    public static synchronized void register(SuggerisciListener listener) {
        listeners.add(listener);
    }

    public static void broadcast(Gruppo g, final String message) {
        for (SuggerisciListener listener : listeners) {
            listener.receiveBroadcast(g, message);
        }
    }

    public static List<SuggerisciListener> getListeners() {
        return listeners;
    }

}
