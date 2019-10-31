package com.example.demo.users.broadcaster;

import com.example.demo.entity.Account;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Broadcaster  {
    static Executor executor = Executors.newSingleThreadExecutor();
    static Map<Account, BroadcastListener> listeners = new HashMap();  //mappa un account ad ogni listner
    static Map<Account, String> accountList = new HashMap<>(); //mappa un account ad un determinato gioco scelto dal teacher
    static int in = 0;

    public static synchronized Registration register(Account account, BroadcastListener broadcastListener) {
        accountList.put(account, "");
        listeners.put(account, broadcastListener);
        System.out.println("Test: Sono il broadcaster ed e' stato chiamato register "+ listeners.size()+ "  ui:"+ broadcastListener);
        return () -> {
            synchronized (Broadcaster.class) {
                listeners.remove(account);
            }
        };
    }

    public static synchronized void unregister(Account account, BroadcastListener broadcastListener){
        listeners.remove(account,broadcastListener);
        accountList.remove(account);
        System.out.println("Broadcaster.Unregister size accountList:" + accountList.size());
    }

    public static synchronized void addUsers(UI ui){
        try {
            listeners.forEach((account, broadcastListener) -> {
                executor.execute(() -> {
                    broadcastListener.addUsers(ui,in);
                });
            });
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static synchronized void updateNumberofConnectedUser(UI ui){
        try {
            listeners.forEach((account, broadcastListener) -> {
                executor.execute(() -> {
                    broadcastListener.countUser(ui,account.getNome());
                });
            });
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }


    public static Map<Account, BroadcastListener> getListeners() {
        return listeners;
    }

    public static Map<Account, String> getAccountList() {
        return accountList;
    }

    public static synchronized void logOut(Account account){
        listeners.remove(account);
        accountList.remove(account);
        System.out.println("Broadcaster.logOut size accountList:" + accountList.size());
    }

    public static int getIn() {
        return in;
    }
}
