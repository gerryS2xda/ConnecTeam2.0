package com.example.demo.users.broadcaster;

import com.example.demo.entity.Account;
import com.example.demo.users.event.AccountListEvent;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.shared.Registration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Broadcaster  {

    //private static field
    private static boolean isGuessStart = false;
    private static boolean isMatyStart = false;

    //static field
    static Executor executor = Executors.newSingleThreadExecutor();
    static Map<Account, BroadcastListener> listeners = new HashMap();  //mappa un account ad ogni listener (per gli studenti)
    static Map<Account, BroadcastListenerTeacher> gestStudlisteners = new HashMap();  //per GestioneStudentUI (usato per ricevere eventi su accountlist)
    static Map<Account, BroadcastListenerTeacher> teacherlisteners = new HashMap(); //mappa un teacher account ad TeacherMainUITab
    static Map<Account, String> accountList = new HashMap<>(); //mappa un account ad un determinato gioco scelto dal teacher
    static Map<Account, String> accountListReceive = new HashMap<>(); //lista di account ricevuti dall'event
    static WrappedSession teacherSession; //memorizza la sessione del teacher per GuessUI e MatyUI (no memorizzazione stato partita)
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

    public static synchronized void logOut(Account account){
        if(account.getTypeAccount().equals("student"))
            listeners.remove(account);
            accountList.remove(account);
        if(account.getTypeAccount().equals("teacher"))
            gestStudlisteners.remove(account); //logout del teacher
        if(countGuessUser > 0)
            countGuessUser--;
        if(countMatyUser > 0)
            countMatyUser--;
        System.out.println("Broadcaster (User)- logOut: size accountList:" + accountList.size());
    }

    //static method for teacher (TeacherMainUITab)
    public static synchronized Registration registerTeacher(Account account, BroadcastListenerTeacher broadcastListener) {
        teacherlisteners.put(account, broadcastListener);
        System.out.println("Broadcaster User: chiamato registerTeacher "+ teacherlisteners.size()+ "  ui:"+ broadcastListener);
        return () -> {
            synchronized (Broadcaster.class) {
                teacherlisteners.remove(account);
            }
        };
    }

    public static synchronized void unregisterTeacher(Account account, BroadcastListenerTeacher broadcastListener){
        teacherlisteners.remove(account,broadcastListener);
    }

    public static synchronized void startGameTeacherInBackground(String game){
        teacherlisteners.forEach((account, broadcastListener) ->{
            executor.execute(()->{
                broadcastListener.startGameInBackground(game);
            });
        });
    }

    public static synchronized void showDialogFinePartitaTeacher(String game){
        teacherlisteners.forEach((account, broadcastListener) ->{
            executor.execute(()->{
                broadcastListener.showDialogFinePartitaTeacher(game);
            });
        });
    }

    public static void setTeacherSession(WrappedSession session){
        teacherSession = session;
    }

    public static WrappedSession getTeacherSession(){
        return teacherSession;
    }

    //static method for GestioneStudentUI
    public static synchronized Registration registerTeacherForGestStud(Account account, BroadcastListenerTeacher broadcastListener) {
        gestStudlisteners.put(account, broadcastListener);
        System.out.println("Broadcaster User: chiamato registerTeacherForGestStud "+ gestStudlisteners.size()+ "  ui:"+ broadcastListener);
        return () -> {
            synchronized (Broadcaster.class) {
                gestStudlisteners.remove(account);
            }
        };
    }

    public static synchronized void unregisterTeacherForGestStud(Account account){
        gestStudlisteners.remove(account); //se si usa una pagina dedicata per GestioneStudentUI occorre passare anche il broadcastListener
    }

    public static synchronized void setAccountListReceive(AccountListEvent event){
        accountListReceive = event.getAccountList();
    }

    public static Map<Account, String> getAccountListReceive() {
        return accountListReceive;
    }

    public static synchronized void updateListaUtentiConnessi(){
        gestStudlisteners.forEach((account, broadcastListener) -> {
           executor.execute(() ->{
               broadcastListener.updateAndMergeAccountList();
           });
        });
    }

    public static synchronized void removeAccountFromAllGrid(Account a){
        gestStudlisteners.forEach((account, broadcastListener) ->{
            executor.execute(()->{
               broadcastListener.removeAccountFromAllGrid(a);
            });
        });
    }

    public static synchronized void removeAccountFromThisGrid(Account a, String nameGrid){
        gestStudlisteners.forEach((account, broadcastListener) ->{
            executor.execute(()->{
                broadcastListener.removeAccountFromThisGrid(a, nameGrid);
            });
        });
    }

    //getter and setter method
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

    public static int getIn() {
        return in;
    }

    public static Map<Account, BroadcastListenerTeacher> getTeacherListeners() {
        return teacherlisteners;
    }

    public static boolean isGuessStart() {
        return isGuessStart;
    }

    public static void setIsGuessStart(boolean isGuessStart) {
        Broadcaster.isGuessStart = isGuessStart;
    }

    public static boolean isMatyStart() {
        return isMatyStart;
    }

    public static void setIsMatyStart(boolean isMatyStart) {
        Broadcaster.isMatyStart = isMatyStart;
    }

    public static void resetCounterUserGame(){
        countGuessUser = 0;
        countMatyUser = 0;
    }
}
