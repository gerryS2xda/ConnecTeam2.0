package com.example.demo.games.maty.gameMenagement.backend.broadcaster;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.games.maty.gameMenagement.backend.MatyController;
import com.example.demo.games.maty.gameMenagement.backend.db.ItemMaty;
import com.example.demo.games.maty.gameMenagement.backend.listeners.BroadcastListenerMaty;
import com.vaadin.flow.shared.Registration;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BroadcasterMaty implements Serializable {

    private static Executor executor = Executors.newSingleThreadExecutor();
    private static Map<Account, BroadcastListenerMaty> listeners = new HashMap();
    private static List<Account> accountList = new ArrayList<>();
    private static int indiziRicevuti = 0;
    private static ArrayList<ItemMaty> items = new ArrayList<ItemMaty>();
    private static List<MatyController.PartitaThread> partiteThread = new ArrayList<MatyController.PartitaThread>();
    private static int in = 0;
    private static ArrayList<Integer> integers = new ArrayList<Integer>();
    private static ArrayList<Integer> contClick = new ArrayList<Integer>();

    //static methods
    public static synchronized Registration register(Account account, BroadcastListenerMaty broadcastListener) {
        accountList.add(account);
        listeners.put(account, broadcastListener);
        System.out.println("BroadcasterMaty.register(): ListenersSize: "+ listeners.size()+ " Account: " + account.getNome() +" UI:"+ broadcastListener);

        return () -> {
            synchronized (BroadcasterMaty.class) {
                listeners.remove(account);
            }
        };
    }

    public static synchronized void unregister(Account account, BroadcastListenerMaty broadcastListener){
        System.out.println("BroadcasterMaty.unregister()");
        listeners.remove(account,broadcastListener);
        accountList.remove(account);

    }

    public static synchronized void startGame(MatyController.PartitaThread partitaThread){
        partiteThread.add(partitaThread);
        listeners.forEach((account, broadcastListener) -> {
            executor.execute(() -> {
                broadcastListener.startGame1();
            });
        });
    }

    public static synchronized void addItemMaty(ItemMaty item){
        items.add(item);
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

    public static synchronized void numeroDaSottrarre(String numero,String numOriginale){
        listeners.forEach((aLong, broadcastListener) -> {
            executor.execute(()-> {
                broadcastListener.numeroDaSotrarre(numero,numOriginale);
            });
        });
    }

    public static synchronized void numeroDASommare(String numOriginale){
        listeners.forEach((aLong, broadcastListener) -> {
            executor.execute(()-> {
                broadcastListener.numeroDaSommare(numOriginale);
            });
        });
    }

    public static synchronized void countDown(int time){
        listeners.forEach((aLong, broadcastListener) -> {
            executor.execute(()-> {
                broadcastListener.countDown(time);
            });
        });
    }

    public static synchronized void addContClick(){
        contClick.add(new Integer(1));
    }

    public static synchronized void addIntegers(Integer integer){
        integers.add(integer);
    }

    public static synchronized void partitaVincente(Gruppo gruppo, String s, Integer integer) {
        try {
            listeners.forEach((account, broadcastListener) -> {
                for(Account i : gruppo.getMembri()){
                    if(i.equals(account)){
                        //Esegui solo per gli account che sono membri del gruppo in cui e' stata trovata la soluzione
                        executor.execute(() -> {
                            broadcastListener.partitaVincente(s,integer);
                        });
                    }
                }
                if(account.getTypeAccount().equals("teacher")){
                    broadcastListener.partitaVincenteForTeacher(gruppo);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void partitanonVincente(Gruppo gruppo) {
        try {
            listeners.forEach((account, broadcastListener) -> {
                for(Account i : gruppo.getMembri()) {
                    if (i.equals(account)) {
                        //Esegui solo per gli account che sono membri del gruppo in cui non e' stata trovata la soluzione
                        executor.execute(() -> {
                            broadcastListener.partitaNonVincente();
                        });
                    }
                }
                if(account.getTypeAccount().equals("teacher")){
                    broadcastListener.partitaNonVincenteForTeacher(gruppo);
                }
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

    public static synchronized void terminaPartitaForAll(String msgDialog){
        try {
            listeners.forEach((account, broadcastListener) -> {
                executor.execute(() -> {
                    broadcastListener.terminaPartitaForAll(msgDialog);
                });
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //getter and setter methods
    public static ArrayList<Integer> getIntegers() {
        return integers;
    }

    public static ArrayList<Integer> getContClick() {
        return contClick;
    }

    public static Map<Account, BroadcastListenerMaty> getListeners() {
        return listeners;
    }

    public static List<Account> getAccountList() {
        return accountList;
    }

    public static int getIndiziRicevuti() {
        return indiziRicevuti;
    }

    public static void setIndiziRicevuti(int indiziRicevuti) {
        BroadcasterMaty.indiziRicevuti = indiziRicevuti;
    }

    public static ArrayList<ItemMaty> getItems() {
        return items;
    }

    public static List<MatyController.PartitaThread> getPartiteThread() {
        return partiteThread;
    }

    public static int getIn() {
        return in;
    }
}
