package com.example.demo.guess.gamesMenagemet.backend.broadcaster;

import com.example.demo.entity.Account;
import com.example.demo.guess.gamesMenagemet.backend.GuessController;
import com.example.demo.guess.gamesMenagemet.backend.db.Item;
import com.example.demo.guess.gamesMenagemet.backend.listeners.BroadcastListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Broadcaster implements Serializable {

    private static Executor executor = Executors.newSingleThreadExecutor();
    private static Map<Account, BroadcastListener> listeners = new HashMap();
    private static List<Account> accountList = new ArrayList<>();
    private static Map<String,Integer> votes = new HashMap();
    private static ArrayList<String> strings = new ArrayList<>();
    private static int indiziRicevuti = 0;
    private static ArrayList<Item> items = new ArrayList<>();
    private static List<GuessController.PartitaThread> partiteThread = new ArrayList<>();
    private static int in = 0;

    public static synchronized Registration register(Account account, BroadcastListener broadcastListener) {
        accountList.add(account);
        listeners.put(account,broadcastListener);
        System.out.println("BroadcasterGuess.register(): ListenersSize: "+ listeners.size()+ "  UI:"+ broadcastListener);
        return () -> {
            synchronized (Broadcaster.class) {
                listeners.remove(account);
            }
        };
    }

    public static synchronized void unregister(Account account, BroadcastListener broadcastListener){
        System.out.println("BroadcasterGuess.unregister()");
        listeners.remove(account,broadcastListener);
        accountList.remove(account);
    }

    public static synchronized void startGame(UI ui, GuessController.PartitaThread partitaThread, Item item){
        items.add(item);
        partiteThread.add(partitaThread);
        listeners.forEach((account, broadcastListener) -> {
            executor.execute(() -> {
                broadcastListener.startGame1(ui);
            });
        });
    }

    public static synchronized void riceveIndizio( final String message) {
        System.out.println("Indizio nel broadcaster: "+ message);
        indiziRicevuti++;
        listeners.forEach((aLong, broadcastListener) -> {
            executor.execute(()-> {
                broadcastListener.receiveIndizio(message);
            });
        });
    }

    public static synchronized void countDown(String n){
        listeners.forEach((aLong, broadcastListener) -> {
            executor.execute(()-> {
                broadcastListener.countDown(n);
            });
        });
    }

    public static synchronized void aggiornaUtentiConnessi(UI ui){
        try {
            listeners.forEach((account, broadcastListener) -> {
                executor.execute(() -> {
                    broadcastListener.countUser(ui,account.getNome());
                });
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static synchronized void addUsers(UI ui){
        try {
            listeners.forEach((account, broadcastListener) -> {
                executor.execute(() -> {
                    broadcastListener.addUsers(ui,in);
                });
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static synchronized void getVotoParola(Map<String, Integer> stringIntegerMap) {
        try {
            votes = stringIntegerMap;
            listeners.forEach((account, broadcastListener) -> {
                executor.execute(() -> {
                    broadcastListener.parolaVotata();
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void addString(String s){
        strings.add(s);
    }

    public static synchronized void partitaVincente(String s, Integer integer) {
        try {
            listeners.forEach((account, broadcastListener) -> {
                executor.execute(() -> {
                    broadcastListener.partititaVincente(s,integer);
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void partitanonVincente() {
        try {
            listeners.forEach((account, broadcastListener) -> {
                executor.execute(() -> {
                    broadcastListener.partititanonVincente();
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void clearPartiteThread(){
        int i = 0;
        try {
            while(i < getPartiteThread().size()){
                getPartiteThread().get(i).interrupt();
                getPartiteThread().get(i).stopTimer();
                i++;
            }
            if(i == getPartiteThread().size()){ //tutti i thread legati alle partite sono terminati
                getPartiteThread().clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void terminaPartitaFromTeacher(){
        try {
            listeners.forEach((account, broadcastListener) -> {
                executor.execute(() -> {
                    broadcastListener.terminaPartitaFromTeacher();
                });
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //getter and setter methods
    public static ArrayList<String> getStrings() {
        return strings;
    }

    public static List<Account> getAccountList() {
        return accountList;
    }

    public static Map<Account, BroadcastListener> getListeners() {
        return listeners;
    }

    public static Map<String, Integer> getVotes() {
        return votes;
    }

    public static int getIndiziRicevuti() {
        return indiziRicevuti;
    }

    public static void setIndiziRicevuti(int indiziRicevuti) {
        Broadcaster.indiziRicevuti = indiziRicevuti;
    }

    public static ArrayList<Item> getItems() {
        return items;
    }

    public static List<GuessController.PartitaThread> getPartiteThread() {
        return partiteThread;
    }

    public static int getIn() {
        return in;
    }
}
