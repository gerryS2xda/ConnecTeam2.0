package com.example.demo.users.event;

import com.example.demo.entity.Account;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Map;

public class AccountListEvent extends ApplicationEvent {

    //instance field
    private Map<Account, String> accountList;
    private String operationName;

    public AccountListEvent(Object source, Map<Account, String> accounts, String operationName){
        super(source);
        accountList = accounts;
        this.operationName = operationName;
    }

    public Map<Account, String> getAccountList(){
        return accountList;
    }

    public String getOperationName(){
        return operationName;
    }

}
