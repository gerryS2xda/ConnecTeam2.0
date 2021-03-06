package com.example.demo.users.event;

import com.example.demo.entity.Account;
import com.example.demo.users.broadcaster.Broadcaster;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
public class AccountListEventListener {

    @Async
    @EventListener
    public void handleReceiveAccountListEvent(AccountListEvent event){
        System.out.println("AccountListEventListener: receive an event");

        Broadcaster.setAccountListReceive(event);
        if(event.getOperationName().equals("add")){
            Broadcaster.updateListaUtentiConnessi();
        }else if(event.getOperationName().equals("remove")){
            for(Account i : event.getAccountList()){
                Broadcaster.removeAccountFromAllGrid(i);
            }
        }

    }
}
