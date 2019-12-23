package com.example.demo.entity;

/*
 * NOTA: Questa classe non e' una entity ma e' un normale JavaBean (cioe' un POJO)
 * Dovra' essere una entity se si implementa la 'Memorizzazione dello stato'
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CronologiaNumeri {

    //instance field
    private Account account; //'account' di colui che ha inserito / rimosso un numero
    private String nomeAccount;
    private Gruppo gruppo; //'gruppo' a cui appartiene l'account che ha inserito / rimosso un numero
    private Component numeroAttuale; //'html component' relativo al numero che e' stato appena inserito
    private List<Integer> numeriEliminatiList; //numeri che sono stati eliminati

    //Constructor
    public CronologiaNumeri(){
        account = new Account();
        nomeAccount = "";
        gruppo = new Gruppo();
        numeroAttuale = new Div(); //un qualsiasi oggetto che e' sottoclasse di 'Component'
        numeriEliminatiList = new ArrayList<Integer>();
    }

    public CronologiaNumeri(Account account){
        this.account = account;
        nomeAccount = account.getNome();
        gruppo = new Gruppo();
        numeroAttuale = new Div(); //un qualsiasi oggetto che e' sottoclasse di 'Component'
        numeriEliminatiList = new ArrayList<Integer>();
    }

    public CronologiaNumeri(Account account, Gruppo gruppo, Component numeroAttuale){
        this.account = account;
        nomeAccount = account.getNome();
        this.gruppo = gruppo;
        this.numeroAttuale = numeroAttuale;
        numeriEliminatiList = new ArrayList<Integer>();
    }

    //getter and setter
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Gruppo getGruppo() {
        return gruppo;
    }

    public void setGruppo(Gruppo gruppo) {
        this.gruppo = gruppo;
    }

    public Component getNumeroAttuale() {
        return numeroAttuale;
    }

    public String getNumeroAttualeWithHTML() {
        return numeroAttuale.getElement().getOuterHTML();
    }

    public void setNumeroAttuale(Component numeroAttuale) {
        this.numeroAttuale = numeroAttuale;
    }

    public List<Integer> getNumeriEliminatiList() {
        return numeriEliminatiList;
    }

    public String getNumeriEliminatiListWithString() {

        String str = "";
        for(Integer i : numeriEliminatiList){
            str = str + i + ",";
        }

        return str;
    }

    public void setNumeriEliminatiList(List<Integer> numeriEliminatiList) {
        this.numeriEliminatiList = numeriEliminatiList;
    }

    public String getNomeAccount() {
        return nomeAccount;
    }

    public void setNomeAccount(String nomeAccount) {
        this.nomeAccount = nomeAccount;
    }

    //Altri metodi
    @Override
    public boolean equals(Object o) { //confronta usando solo 'Account'
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CronologiaNumeri that = (CronologiaNumeri) o;
        return Objects.equals(account, that.account);
    }

}
