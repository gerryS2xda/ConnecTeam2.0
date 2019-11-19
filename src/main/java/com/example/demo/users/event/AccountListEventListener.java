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
        System.out.println("EventListenerAccountTest: sono stato chiamato");
        Broadcaster.setAccountListReceive(event);
        Broadcaster.updateListaUtentiConnessi();
    }
}
