package com.example.demo.users.event;

import com.example.demo.entity.Account;
import org.springframework.context.ApplicationEvent;
import java.util.List;

public class AccountListEvent extends ApplicationEvent {

    //instance field
    private List<Account> accountList;
    private String operationName;

    public AccountListEvent(Object source, List<Account> accounts, String operationName){
        super(source);
        accountList = accounts;
        this.operationName = operationName;
    }

    public List<Account> getAccountList(){
        return accountList;
    }

    public String getOperationName(){
        return operationName;
    }

}
