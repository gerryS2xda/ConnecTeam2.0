package com.example.demo.users.event;

import com.example.demo.entity.Account;

import java.util.Map;

public interface StartGameEventBeanListener {
    Map<Account, String> getAccountList();
    boolean isPartitaStart();
}
