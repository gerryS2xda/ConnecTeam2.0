package com.example.demo.entity;

/*
 * NOTA: Questa classe non e' una entity ma e' un normale JavaBean (cioe' un POJO)
 * Dovra' essere una entity se si implementa la 'Memorizzazione dello stato'
 */

import com.vaadin.flow.component.Component;

import java.util.*;

public class Gruppo {

    //instance field
    private String id;  //es. Gruppo 1
    private String nomeGioco;
    private List<Account> membri;
    private Map<Account, Component> azioniAccount; //mappa le azioni (memorizzate come 'Component') all'Account che le ha commesse

    //Constructor
    public Gruppo(){
        id = "";
        nomeGioco = "";
        membri = new ArrayList<Account>();
        azioniAccount = new HashMap<>();
    }

    public Gruppo(String id, String nomeGioco, List<Account> membri, Map<Account, Component> azioniAccount) {
        this.id = id;
        this.nomeGioco = nomeGioco;
        this.membri = membri;
        this.azioniAccount = azioniAccount;
    }

    //getter and setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeGioco() {
        return nomeGioco;
    }

    public void setNomeGioco(String nomeGioco) {
        this.nomeGioco = nomeGioco;
    }

    public List<Account> getMembri() {
        return membri;
    }

    public void setMembri(List<Account> membri) {
        this.membri = membri;
    }

    public Map<Account, Component> getAzioniAccount() {
        return azioniAccount;
    }

    public void setAzioniAccount(Map<Account, Component> azioniAccount) {
        this.azioniAccount = azioniAccount;
    }

    //toString() and equals
    @Override
    public String toString() {
        return "Gruppo{id=" + id + ", nomeGioco='" + nomeGioco + '\'' +
                ", membri=" + membri + ", azioniAccount=" + azioniAccount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gruppo gruppo = (Gruppo) o;
        return id == gruppo.id && Objects.equals(nomeGioco, gruppo.nomeGioco);
    }

}
