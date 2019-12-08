package com.example.demo.utility;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;

import java.util.List;

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
}
