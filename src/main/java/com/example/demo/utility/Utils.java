package com.example.demo.utility;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {

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

    public static Component getComponentByElementId(Map<Account, Component> azioni, String valueId){
        Component c = null;

        for(Account i : azioni.keySet()){
            String str = azioni.get(i).getElement().getAttribute("id");
            if(str.equals(valueId)){
                c = azioni.get(i);
                break;
            }
        }
        return c;
    }

    public static ArrayList<Component> getComponentsByElementId(Map<Account, Component> azioni, String valueId){
        ArrayList<Component> list = new ArrayList<Component>();

        for(Account i: azioni.keySet()){
            String str = azioni.get(i).getElement().getAttribute("id");
            if(str.equals(valueId)){
                list.add(azioni.get(i));
            }
        }
        return list;
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
}
