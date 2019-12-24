package com.example.demo.entity;

/*
 * NOTA: Questa classe non e' una entity ma e' un normale JavaBean (cioe' un POJO)
 * Dovra' essere una entity se si implementa la 'Memorizzazione dello stato'
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CronologiaNumeri {

    //instance field
    private Account account; //'account' di colui che ha inserito / rimosso un numero
    private String nomeAccount;
    private Gruppo gruppo; //'gruppo' a cui appartiene l'account che ha inserito / rimosso un numero
    private int numeroInserito;
    private List<Integer> numeriEliminatiList; //numeri che sono stati eliminati

    //Constructor
    public CronologiaNumeri(){
        account = new Account();
        nomeAccount = "";
        gruppo = new Gruppo();
        numeroInserito = 0;
        numeriEliminatiList = new ArrayList<Integer>();
    }

    public CronologiaNumeri(Account account){
        this.account = account;
        nomeAccount = account.getNome();
        gruppo = new Gruppo();
        numeroInserito = 0;
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

    public int getNumeroInserito() {
        return numeroInserito;
    }

    public void setNumeroInserito(int numeroInserito) {
        this.numeroInserito = numeroInserito;
    }

    public List<Integer> getNumeriEliminatiList() {
        return numeriEliminatiList;
    }

    public String getNumeriEliminatiListWithString() {

        //Rimuovi elementi se 'numeriEliminatiList' supera una certa dimensione e poi stampa
        if(numeriEliminatiList.size() > 4){
            numeriEliminatiList.remove(0);
        }

        //Costruisci stringa da mostrare nella grid
        String str = "";
        for(int i = 0; i < numeriEliminatiList.size(); i++ ){
            if(i == 0){
                str += "" + numeriEliminatiList.get(i);
                continue;
            }
            str += ", " + numeriEliminatiList.get(i);
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
