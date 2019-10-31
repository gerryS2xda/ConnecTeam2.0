package com.example.demo.users.event;

import com.example.demo.entity.Account;

import java.util.List;
import java.util.Map;

public interface AccountListEventBeanListener {
    Map<Account, String> getAccountList();
}
