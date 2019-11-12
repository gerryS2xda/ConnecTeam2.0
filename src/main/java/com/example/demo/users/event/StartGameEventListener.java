package com.example.demo.users.event;

import com.example.demo.entity.Account;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StartGameEventListener implements ApplicationListener<StartGameEvent>, StartGameEventBeanListener {

    private Map<Account, String> accountList = new HashMap<>();
    private boolean isStartPartita = false;

    @Override
    public void onApplicationEvent(StartGameEvent event){
        System.out.println("Receive a custom event: " + event.getAccountList());
        accountList = event.getAccountList();
        isStartPartita = event.isStartPartita();
    }

    public Map<Account, String> getAccountList(){
        return accountList;
    }

    public boolean isPartitaStart(){
        return isStartPartita;
    }

}
