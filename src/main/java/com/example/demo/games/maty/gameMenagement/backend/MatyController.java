package com.example.demo.games.maty.gameMenagement.backend;


import com.example.demo.entity.Account;
import com.example.demo.entity.Partita;
import com.example.demo.entity.Punteggio;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.games.maty.gameMenagement.backend.broadcaster.BroadcasterMaty;
import com.example.demo.games.maty.gameMenagement.backend.db.ItemMaty;
import com.example.demo.games.maty.gameMenagement.backend.db.ItemRepositoryMaty;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

@Component
@VaadinSessionScope
@Lazy
public class MatyController {

    private ItemRepositoryMaty itemRepository;
    private ItemMaty item;
    private Random random= new Random();
    private PartitaThread partitaThread;
    private List<Account> accounts;
    private int i;
    private int totTime;
    private boolean vinta = false;
    private PartitaRepository partitaRepository;
    private WrappedSession teacherSession;
    protected Partita partita;

    public MatyController(PartitaRepository partitaRepository){
        this.partitaRepository= partitaRepository;
        teacherSession = com.example.demo.users.broadcaster.Broadcaster.getTeacherSession();

        if(VaadinService.getCurrentRequest() != null) {
            //Ottieni valori dalla sessione corrente e verifica se sono presenti in sessione
            itemRepository = (ItemRepositoryMaty) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("itemRepositoryMaty");
        }else{ //getCurrentRequest() is null (poiche' e' il server che 'impone' accesso a questa pagina - no memorizzazione stato partita)
            itemRepository = (ItemRepositoryMaty) teacherSession.getAttribute("itemRepositoryMaty");
        }
    }

    protected void addPunteggio(Punteggio punteggio){
        partita.addPunteggio(punteggio);
    }

    public void setItemMatyBeforeStartGame(){
        int tot = itemRepository.numeroRighe();
        int rand = random.nextInt(tot)+1;
        item = itemRepository.findOneById(rand);
    }

    public void startGame(Partita parita){

        this.partita = parita;

        if(partitaThread!=null){
            partitaThread.interrupt();
            partitaThread.stopTimer();
        }
        partitaThread=new PartitaThread();
        partitaThread.start();
    }

    public ItemMaty getItem() {
        return item;
    }

    public boolean partitaVincente(String parolaVincente,ItemMaty item){

        if(parolaVincente.equalsIgnoreCase(item.getParola())) {
            vinta = true;
            partita = new Partita(new Timestamp(new Date().getTime()), "Maty");
            partita.setVinta(true);
            accounts = BroadcasterMaty.getAccountList();
            for (Account a : accounts) {
                if (BroadcasterMaty.getIndiziRicevuti() == 0) {
                    addPunteggio(new Punteggio(a, 120));
                } else if (BroadcasterMaty.getIndiziRicevuti() == 1) {
                    addPunteggio(new Punteggio(a, 100));
                } else if (BroadcasterMaty.getIndiziRicevuti() == 2) {
                    addPunteggio(new Punteggio(a, 60));
                } else if (BroadcasterMaty.getIndiziRicevuti() == 3) {
                    addPunteggio(new Punteggio(a, 30));
                } else if (BroadcasterMaty.getIndiziRicevuti() == 4) {
                    addPunteggio(new Punteggio(a, 10));
                }
            }
            partitaRepository.save(partita);
            BroadcasterMaty.setIndiziRicevuti(0);
        }else {
            vinta = false;
            partita= new Partita(new Timestamp(new Date().getTime()), "Maty");
            partita.setVinta(false);
            accounts = BroadcasterMaty.getAccountList();
            for (Account a: accounts) {
                addPunteggio(new Punteggio(a,0));
            }
            partitaRepository.save(partita);
        }



        return vinta;
    }

    public PartitaThread getPartitaThread() {
        return partitaThread;
    }

    public class PartitaThread extends Thread{
        private Timer timer;
        @Override
        public void run() {
            timer = new Timer();
            totTime = 300;
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    BroadcasterMaty.countDown(totTime);
                    totTime--;
                    if (totTime < 0) {
                        timer.cancel();
                        terminaPartita();
                    }
                }
            }, 0, 1000);

            return;
        }

        public void terminaPartita(){
            partita= new Partita(new Timestamp(new Date().getTime()), "Maty");
            accounts = BroadcasterMaty.getAccountList();
            BroadcasterMaty.setIndiziRicevuti(0);
            for (Account a: accounts) {
                addPunteggio(new Punteggio(a,5));
            }
            partitaRepository.save(partita);
            System.out.println("Fine Partita");
            partitaThread.interrupt();
            partitaThread.stopTimer();
            BroadcasterMaty.setIndiziRicevuti(0);
            BroadcasterMaty.terminaPartitaForAll("Partita terminata!!"); //usato per indicare che la partita deve terminare per tutti
        }

        public void stopTimer(){
            timer.cancel();
        }
    }


}
