package com.example.demo.users.event;

import com.example.demo.entity.Account;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AccountListEventListener implements ApplicationListener<AccountListEvent>, AccountListEventBeanListener {

    private Map<Account, String> accountList = new HashMap<>();

    @Override
    public void onApplicationEvent(AccountListEvent event){
        System.out.println("Receive a custom event: " + event.getAccountList());
        accountList = event.getAccountList();
        for(Account a : accountList.keySet()){
            System.out.println("Key: " + a.toString() + "\nGame: " + accountList.get(a));
        }
    }

    public Map<Account, String> getAccountList(){
        return accountList;
    }

}
