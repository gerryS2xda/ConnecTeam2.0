package com.example.demo.nuovoGioco.gameManagement.database;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@Lazy
public interface ItemRepositoryNuovoGioco extends JpaRepository<ItemNuovoGioco, Integer> {
    ItemNuovoGioco findOneById(int id);
}