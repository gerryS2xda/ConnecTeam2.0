package com.example.demo.utility;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class Utils {

    //Utils per 'Gruppo'
    public static Gruppo findGruppoByAccount(List<Gruppo> gruppi, Account account){
        Gruppo x = new Gruppo();

        for(Gruppo g : gruppi){
            for(Account a : g.getMembri()){
                if(a.equals(account)){
                    x = g;
                    break;
                }
            }
        }

        return x;
    }

    public static boolean isAccountInThisGruppo(Gruppo g, Account account){
        boolean b = false;
        for(Account i : g.getMembri()){
            if(i.equals(account)){
                b = true;
                break;
            }
        }
        return b;
    }

    public static Gruppo findGruppoByName(List<Gruppo> gruppi, String nome){
        Gruppo x = new Gruppo();
        for(Gruppo g : gruppi){
            if(g.getId().equals(nome)){
                x = g;
                break;
            }
        }
        return x;
    }

    public static Div getDivFromListByAttribute(ArrayList<Div> list, String attr, String value){
        Div d = new Div();

        for(Div i : list){
            if(i.getElement().getAttribute(attr).equals(value)){
                d = i;
                break;
            }
        }

        return d;
    }

    public static VerticalLayout getVerticalLayoutFromListByAttribute(ArrayList<VerticalLayout> list, String attr, String value){
        VerticalLayout d = new VerticalLayout();

        for(VerticalLayout i : list){
            if(i.getElement().getAttribute(attr).equals(value)){
                d = i;
                break;
            }
        }

        return d;
    }

    public static MessageList getMessageListFromListByAttributeForChat(ArrayList<MessageList> list, String attr, String value){
        MessageList d = new MessageList("chatlayoutmessage2");

        for(MessageList i : list){
            if(i.getElement().getAttribute(attr).equals(value)){
                d = i;
                break;
            }
        }

        return d;
    }

    public static boolean removeAccountFromGroupAndCheckIfGroupIsEmpty(List<Gruppo> gruppi, Account account){
        boolean flag = false; //true se un gruppo, in base ad account, non ha piu' membri.

        Gruppo g = findGruppoByAccount(gruppi, account);
        g.getMembri().remove(account);
        if(g.getMembri().size() == 0){
            flag = true;
        }
        return flag;
    }

    //Altri metodi
    public static Set<Account> cloneListAccountsWithoutDuplicate(List<Account> list){
        List<Account> accounts = new ArrayList<Account>();
        for(Account i : list){
            accounts.add(i);
        }
        //NOTA: CopyOnWriteArraySet permette di evitare 'ConcurrentModificationException' poiche' e' thread-safe
        return new CopyOnWriteArraySet<Account>(accounts); //rimuove i duplicati poiche' e' un insieme (Set)
    }
}
