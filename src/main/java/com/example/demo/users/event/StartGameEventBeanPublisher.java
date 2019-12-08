package com.example.demo.users.event;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;

import java.util.List;
import java.util.Map;

public interface StartGameEventBeanPublisher {
    void doStuffAndPublishAnEvent(List<Gruppo> eventContent, String nomeGioco);
}
