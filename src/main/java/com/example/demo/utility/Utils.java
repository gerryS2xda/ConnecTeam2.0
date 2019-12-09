package com.example.demo.utility;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.vaadin.flow.component.Component;

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

        System.out.println("Utils: Account: " + account.getNome() + " Gruppo: " + x.getId());

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
}
