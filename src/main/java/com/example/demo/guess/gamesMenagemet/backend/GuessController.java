package com.example.demo.guess.gamesMenagemet.backend;


import com.example.demo.entity.Account;
import com.example.demo.entity.Partita;
import com.example.demo.entity.Punteggio;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.guess.gamesMenagemet.backend.broadcaster.BroadcasterGuess;
import com.example.demo.guess.gamesMenagemet.backend.db.Item;
import com.example.demo.guess.gamesMenagemet.backend.db.ItemRepository;
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
public class GuessController {

    private ItemRepository itemRepository;
    private Item item;
    private Random random= new Random();
    private PartitaThread partitaThread;
    private List<Account> accounts;
    private int i;
    private int totTime;
    private boolean vinta = false;
    private WrappedSession teacherSession;
    private PartitaRepository partitaRepository;
    protected Partita partita;

    public GuessController(PartitaRepository partitaRepository){
        this.partitaRepository= partitaRepository;
        teacherSession = com.example.demo.users.broadcaster.Broadcaster.getTeacherSession();
    }

    protected void addPunteggio(Punteggio punteggio){
        partita.addPunteggio(punteggio);
    }

    public void startGame(Partita partita){

        this.partita = partita;

        if(VaadinService.getCurrentRequest() != null) {
            //Ottieni valori dalla sessione corrente e verifica se sono presenti in sessione
            itemRepository = (ItemRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("itemRepository");
            partitaRepository = (PartitaRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("partitaRepository");
        }else{ //getCurrentRequest() is null (poiche' e' il server che 'impone' accesso a questa pagina - no memorizzazione stato partita)
            itemRepository = (ItemRepository) teacherSession.getAttribute("itemRepository");
            partitaRepository = (PartitaRepository) teacherSession.getAttribute("partitaRepository");
        }

        int tot = itemRepository.numeroRighe();
        int rand = random.nextInt(tot)+1;
        item = itemRepository.findOneById(rand);
        if(partitaThread!=null){
            partitaThread.interrupt();
            partitaThread.stopTimer();
        }
        partitaThread=new PartitaThread();
        partitaThread.start();
    }

    public Item getItem() {
        return item;
    }

    public boolean partitaVincente(String parolaVincente,Item item){
        if(parolaVincente.equalsIgnoreCase(item.getParola())){
            vinta = true;
            partita= new Partita(new Timestamp(new Date().getTime()), "Guess");
            partita.setVinta(true);
            accounts = BroadcasterGuess.getAccountList();
            for (Account a: accounts) {
                if(BroadcasterGuess.getIndiziRicevuti() == 1){
                    addPunteggio(new Punteggio(a,100));
                }else if(BroadcasterGuess.getIndiziRicevuti() == 2){
                    addPunteggio(new Punteggio(a,60));
                }else if(BroadcasterGuess.getIndiziRicevuti() == 3){
                    addPunteggio(new Punteggio(a,30));
                }else if(BroadcasterGuess.getIndiziRicevuti() == 4){
                    addPunteggio(new Punteggio(a,10));
                }
            }
            partitaRepository.save(partita);
        }else {
            vinta = false;
            partita= new Partita(new Timestamp(new Date().getTime()), "Guess");
            partita.setVinta(false);
            accounts = BroadcasterGuess.getAccountList();
            for (Account a: accounts) {
                addPunteggio(new Punteggio(a,0));
            }
            partitaRepository.save(partita);

        }
        BroadcasterGuess.setIndiziRicevuti(0);
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
            i=0;
            String indizio = item.getIndizio(i);
            BroadcasterGuess.riceveIndizio(i, indizio);
            i++;
            totTime = 30;
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    BroadcasterGuess.countDown(totTime);
                    if (totTime == 0 && i<3) {
                        String indizio = item.getIndizio(i);
                        BroadcasterGuess.riceveIndizio(i, indizio);
                        totTime=30;
                        i++;
                    }else if(i==3 && totTime==0){
                        String indizio = item.getIndizio(i);
                        BroadcasterGuess.riceveIndizio(i, indizio);
                        totTime=30;
                        i++;
                    }
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
            partita= new Partita(new Timestamp(new Date().getTime()), "Guess");
            accounts = BroadcasterGuess.getAccountList();
            BroadcasterGuess.setIndiziRicevuti(0);
            for (Account a: accounts) {
                addPunteggio(new Punteggio(a,0));
            }
            partitaRepository.save(partita);
            System.out.println("Fine Partita");
            partitaThread.interrupt();
            partitaThread.stopTimer();
            BroadcasterGuess.terminaPartitaForAll("Partita terminata!!"); //usato per indicare che la partita deve terminare per tutti

        }

        public void stopTimer(){
            timer.cancel();
        }
    }


}
