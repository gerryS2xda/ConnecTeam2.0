package com.example.demo.users.broadcaster;

import com.vaadin.flow.component.UI;

public interface BroadcastListener {

    void addUsers(UI ui, int i);
    void countUser(UI ui, String nome);

}
