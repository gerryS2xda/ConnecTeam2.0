package com.example.demo.chat;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class BroadcasterChat implements Serializable {

    private static final Map<Account, ChatListener> listeners = new HashMap<>();

    public static void register(Account acc, ChatListener listener) {
        listeners.put(acc, listener);
    }

    public static void broadcast(Gruppo g, String message) {
        listeners.forEach((account, chatListener) -> {
            chatListener.receiveBroadcast(g, message);
        });
    }

    public static Map<Account, ChatListener> getListeners() {
        return listeners;
    }
}
