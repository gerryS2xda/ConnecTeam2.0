package com.example.demo.users.broadcaster;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.users.event.AccountListEvent;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Broadcaster  {

    //private static field
    private static boolean isGuessStart = false;
    private static boolean isMatyStart = false;

    //static field
    private static Executor executor = Executors.newSingleThreadExecutor();
    private static Map<Account, BroadcastListener> listeners = new HashMap();  //mappa un account ad ogni listener (per gli studenti)
    private static Map<Account, BroadcastListenerTeacher> gestStudlisteners = new HashMap();  //per GestioneStudentUI (usato per ricevere eventi su accountlist)
    private static Map<Account, BroadcastListenerTeacher> teacherlisteners = new HashMap(); //mappa un teacher account ad TeacherMainUITab
    private static List<Account> accountList = new ArrayList<Account>();
    private static List<Account> accountListReceive = new ArrayList<Account>(); //lista di account ricevuti dall'event
    private static WrappedSession teacherSession; //memorizza la sessione del teacher per GuessUI e MatyUI (no memorizzazione stato partita)
    private static int in = 0;
    private static int countGuessUser = 0;
    private static int countMatyUser = 0;
    private static Map<Account, WebBrowser> accountWB = new HashMap<>(); //associa ogni account a un web browser (necessario per controllo account gia' loggati)
    private static List<Gruppo> gruppiListReceive = new ArrayList<Gruppo>();

    public static synchronized void addNewAccountWithWebBrowser(Account account, WebBrowser wb){
        accountWB.put(account, wb);
    }

    public static synchronized void removeAccountWithWebBrowser(Account account){
        accountWB.remove(account);
    }

    //static methods for discusser (student)
    public static synchronized Registration register(Account account, BroadcastListener broadcastListener) {
        accountList.add(account);
        listeners.put(account, broadcastListener);
        System.out.println("Broadcaster User: chiamato register "+ listeners.size()+ "  Account:"+ account.getNome());
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

    //static methods for teacher (TeacherMainUITab)
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

    //static methods for GestioneStudentUI
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

    public static List<Account> getAccountListReceive() {
        return accountListReceive;
    }

    public static synchronized void updateListaUtentiConnessi(){
        gestStudlisteners.forEach((account, broadcastListener) -> {
           executor.execute(() ->{
               broadcastListener.updateGridStudentCollegati();
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

    //getter and setter method
    public static Map<Account, BroadcastListener> getListeners() {
        return listeners;
    }

    public static List<Account> getAccountList() {
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

    public static void resetFlagIsGameStart(){
        isGuessStart = false;
        isMatyStart = false;
    }

    public static void setCountGuessUser(int countGuessUser) {
        Broadcaster.countGuessUser = countGuessUser;
    }

    public static void setCountMatyUser(int countMatyUser) {
        Broadcaster.countMatyUser = countMatyUser;
    }

    public static Map<Account, WebBrowser> getAccountWithWebBrowserHashMap(){
        return accountWB;
    }

    public static List<Gruppo> getGruppiListReceive() {
        return gruppiListReceive;
    }

    public static void setGruppiListReceive(List<Gruppo> gruppiListReceive) {
        Broadcaster.gruppiListReceive = gruppiListReceive;
    }
}
