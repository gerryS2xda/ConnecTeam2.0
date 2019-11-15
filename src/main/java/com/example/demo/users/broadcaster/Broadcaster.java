package com.example.demo.users.broadcaster;

import com.example.demo.entity.Account;
import com.vaadin.flow.shared.Registration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Broadcaster  {
    static Executor executor = Executors.newSingleThreadExecutor();
    static Map<Account, BroadcastListener> listeners = new HashMap();  //mappa un account ad ogni listner
    static Map<Account, String> accountList = new HashMap<>(); //mappa un account ad un determinato gioco scelto dal teacher
    static int in = 0;
    static int countGuessUser = 0;
    static int countMatyUser = 0;

    public static synchronized Registration register(Account account, BroadcastListener broadcastListener) {
        accountList.put(account, "");
        listeners.put(account, broadcastListener);
        System.out.println("Broadcaster User: chiamato register "+ listeners.size()+ "  ui:"+ broadcastListener);
        return () -> {
            synchronized (Broadcaster.class) {
                listeners.remove(account);
            }
        };
    }

    public static synchronized void unregister(Account account, BroadcastListener broadcastListener){
        listeners.remove(account,broadcastListener);
        accountList.remove(account);
        if(countGuessUser > 0)
            countGuessUser--;
        if(countMatyUser > 0)
            countMatyUser--;
        System.out.println("BroadcasterUSER.Unregister: size accountList:" + accountList.size());
    }

    public static synchronized void redirectToGuess(Account a){
        countGuessUser++;
        try {
            executor.execute(() -> {
                 listeners.get(a).redirectToGuess();
            });
        }catch (Exception e){
            countGuessUser--;
            System.out.println(e.getMessage());
        }
    }

    public static synchronized void redirectToMaty(Account a){
        countMatyUser++;
        try {
            executor.execute(() -> {
                listeners.get(a).redirectToMaty();
            });
        }catch (Exception e){
            countMatyUser--;
            System.out.println(e.getMessage());
        }
    }

    public static Map<Account, BroadcastListener> getListeners() {
        return listeners;
    }

    public static Map<Account, String> getAccountList() {
        return accountList;
    }

    public static int getNumberOfGuessUser(){
        return countGuessUser;
    }

    public static int getNumberOfMatyUser(){
        return countMatyUser;
    }

    public static synchronized void logOut(Account account){
        listeners.remove(account);
        accountList.remove(account);
        if(countGuessUser > 0)
            countGuessUser--;
        if(countMatyUser > 0)
            countMatyUser--;
        System.out.println("Broadcaster (User)- logOut: size accountList:" + accountList.size());
    }

    public static int getIn() {
        return in;
    }
}
