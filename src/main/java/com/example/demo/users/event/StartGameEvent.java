package com.example.demo.users.event;

import com.example.demo.entity.Account;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class StartGameEvent extends ApplicationEvent {

    //instance field
    private Map<Account, String> accountList;
    private boolean isStartPartita = false;

    public StartGameEvent(Object source, Map<Account, String> accounts, boolean flag){
        super(source);
        accountList = accounts;
        isStartPartita = flag;
    }

    public Map<Account, String> getAccountList(){
        return accountList;
    }

    public boolean isStartPartita(){
        return isStartPartita;
    }
}
