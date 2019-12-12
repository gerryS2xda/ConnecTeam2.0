package com.example.demo.guess.gamesMenagemet.frondend;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.entity.Partita;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.games.Guess;
import com.example.demo.guess.gamesMenagemet.backend.GuessController;
import com.example.demo.guess.gamesMenagemet.backend.broadcaster.Broadcaster;
import com.example.demo.guess.gamesMenagemet.backend.broadcaster.BroadcasterChat;
import com.example.demo.guess.gamesMenagemet.backend.broadcaster.BroadcasterSuggerisci;
import com.example.demo.guess.gamesMenagemet.backend.db.Item;
import com.example.demo.guess.gamesMenagemet.backend.listeners.BroadcastListener;
import com.example.demo.guess.gamesMenagemet.backend.listeners.ChatListener;
import com.example.demo.users.event.EndGameEventBeanPublisher;
import com.example.demo.utility.*;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.*;

@Push
@Route("guess")
@HtmlImport("chat.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/guessStyle.css")
@StyleSheet("frontend://stile/chat.css")
@PageTitle("ConnecTeam-Guess")
public class GuessUI extends HorizontalLayout implements BroadcastListener, ChatListener, PageConfigurator {

    //static field
    public static Gruppo currentGroupSelect;

    //instance field
    private Account account;
    private AccountRepository accountRepository;
    private PartitaRepository partitaRepository;
    private Partita partita;
    private GuessController.PartitaThread partitaThread;
    private Item item;
    private Guess guess;
    private GuessController guessController;
    private MessageList messageList = new MessageList("chatlayoutmessage2");
    private Label numeroUtenti = new Label();
    private VerticalLayout chatMessages = new VerticalLayout();
    private Label secondi = new Label();
    private Label indizio = new Label("Indizi: ");
    private VerticalLayout verticalLayout = new VerticalLayout();
    private boolean isStarted = false;
    private Div chat = new Div();
    private Image image333;
    private boolean isTeacher = false;
    private EndGameEventBeanPublisher endGamePublisher;
    //Numero di utenti connessi al momento in cui il teacher da' il via alla partita
    private int maxNumeroStutentiConnessi = 0;
    private WrappedSession teacherSession;
    private Button start; //pulsante che sara'
    private Dialog attendiDialog;
    private List<Gruppo> gruppi = new ArrayList<Gruppo>();
    private ArrayList<Div> containersPVTeacher; //container parole votate per teacher
    private ArrayList<Div> containersParoleVotate; //container parole votate per student
    private StartGameUI startGameUI;

    public GuessUI(@Autowired EndGameEventBeanPublisher endGameEventBeanPublisher) {

        try {
            //Inizializzazione
            setId("GuessUI");
            getStyle().set("height", "100%");
            guess = new Guess();
            endGamePublisher = endGameEventBeanPublisher;
            maxNumeroStutentiConnessi = com.example.demo.users.broadcaster.Broadcaster.getNumberOfGuessUser();
            teacherSession = com.example.demo.users.broadcaster.Broadcaster.getTeacherSession();
            gruppi = com.example.demo.users.broadcaster.Broadcaster.getGruppiListReceive();
            currentGroupSelect = new Gruppo();
            containersPVTeacher = new ArrayList<Div>();
            containersParoleVotate = new ArrayList<Div>();

            if(VaadinService.getCurrentRequest() != null) {
                //Ottieni valori dalla sessione corrente e verifica se sono presenti in sessione
                account = (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
                if (account == null)
                    throw new IllegalArgumentException("GuessUI: Account is null");
                accountRepository = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
                if (accountRepository == null)
                    throw new IllegalArgumentException("GuessUI: AccountRepository is null");
                partitaRepository = (PartitaRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("partitaRepository");
                if (partitaRepository == null)
                    throw new IllegalArgumentException("GuessUI: PartitaRepository is null");
            }else{ //getCurrentRequest() is null (poiche' e' il server che 'impone' accesso a questa pagina - no memorizzazione stato partita)
                account = (Account) teacherSession.getAttribute("user");
                accountRepository = (AccountRepository) teacherSession.getAttribute("rep");
                partitaRepository = (PartitaRepository) teacherSession.getAttribute("partitaRepository");
            }

            if(account.getTypeAccount().equals("teacher")) {
                isTeacher = true;
                //UI.getCurrent().setPollInterval(1000); Per il teacher: da usare solo le pagina viene caricata con UI.navigate(...)
            }else{
                DialogUtility dialogUtility = new DialogUtility();
                attendiDialog = dialogUtility.showDialog("Attendere...", "black");
                attendiDialog.open();
            }
            //Inizializzazione per StartGameUI
            guessController = new GuessController(partitaRepository);
            startGameUI = new StartGameUI(guessController, isTeacher, account);


            //Per ogni partita gia' iniziata, setta isStarted a true (una sola partita alla volta)
            for (int i = 0; i < Broadcaster.getPartiteThread().size(); i++) {
                if (Broadcaster.getPartiteThread().get(i) != null) {
                    isStarted = true;
                }
            }

            if (isStarted != true) {
                Broadcaster.register(account, this);
                BroadcasterChat.register(this);
                Broadcaster.aggiornaUtentiConnessi(UI.getCurrent());
            } else {
                System.out.println("GUESSUI:TEST1 Account: " + account.getNome());
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEvent("C'è una partita in corso aspetta che finisca", "0");
            }

            if(isTeacher){ //mostra la appbar
                getStyle().set("width", "100%");
                AppBarUI appBar = new AppBarUI("Guess", false, true); //nome pagina corrente
                add(appBar);
            }

            //Chat container
            Div device = new Div();
            device.getStyle().set("width", "30%"); //value precedente: 500px
            Label label = new Label("Chat");
            label.getStyle().set("font-size", "30px");
            device.add(label);
            device.setId("device");

            chat.addClassName("chat");

            HorizontalLayout textFieldSendBtn = new HorizontalLayout();
            textFieldSendBtn.setSpacing(false);
            textFieldSendBtn.getStyle().set("margin-top", "12px");
            TextField message1 = new TextField();
            Icon icon = new Icon(VaadinIcon.PAPERPLANE_O);
            icon.setSize("24px");
            icon.setColor("white");
            if(isTeacher)
                icon.getStyle().set("left", "100px");

            Button send = new Button(icon);
            message1.addKeyDownListener(Key.ENTER, keyDownEvent -> {
                String mess = message1.getValue();
                if (!mess.equals("")) {
                    if(account.getTypeAccount().equals("teacher"))
                        BroadcasterChat.broadcast("Teacher: " + message1.getValue()+":"+account.getId());
                    else
                        BroadcasterChat.broadcast(account.getNome() + ": " + message1.getValue()+":"+account.getId());
                    message1.setValue("");
                }
            });
            message1.getStyle().set("width","80%"); //valore precedente: 85%
            message1.getStyle().set("margin-right","16px");
            send.addClickListener(buttonClickEvent -> {
                String mess = message1.getValue();
                if (!mess.equals("")) {
                    if(isTeacher) {
                        BroadcasterChat.broadcast("Teacher: " + message1.getValue() + ":" + account.getId());
                    }else{
                        BroadcasterChat.broadcast(account.getNome() + ": " + message1.getValue()+":"+account.getId());
                    }
                    message1.setValue("");
                }
            });
            send.addClassName("buttonSendChat");
            textFieldSendBtn.add(message1, send);

            chat.add(messageList);
            device.add(chat);
            device.add(textFieldSendBtn);
            add(device);

            for(Gruppo y : gruppi){
                Div d = new Div();
                d.getElement().setAttribute("name", y.getId());
                d.addClassName("containerParoleVotateTeacher");
                d.getStyle().set("display", "none");
                containersPVTeacher.add(d);

                Div d2 = new Div();
                d2.getElement().setAttribute("name", y.getId());
                d2.addClassName("containerParoleVotate");
                d2.getStyle().set("display", "none");
                containersParoleVotate.add(d2);
            }

            //Container nome utente e pulsante 'Info' su Guess
            if(!isTeacher) {
                add(nameUserAndInfoBtnContainer());
            }

            start = new Button();
            start.getStyle().set("display", "none");
            start.addClickListener(buttonClickEvent -> {
                System.out.println("GuessUI: Partita iniziata!");
                for (int i = 0; i < Broadcaster.getPartiteThread().size(); i++) {
                    if (Broadcaster.getPartiteThread().get(i) != null) {
                        isStarted = true;
                    }
                }
                if (isStarted != true) {
                    partita = new Partita(new Timestamp(new Date().getTime()), "Guess");
                    guessController.startGame(partita);
                    partitaThread = guessController.getPartitaThread();
                    item = guessController.getItem();
                    Broadcaster.startGame(UI.getCurrent(), partitaThread, item);
                } else {
                    System.out.println("GUESSUI:TEST2");
                    InfoEventUtility infoEventUtility = new InfoEventUtility();
                    infoEventUtility.infoEvent("C'è una partita in corso aspetta che finisca", "10");
                }
            });
            add(start);

            if(isTeacher){
                showSelectsFieldGruppiForTeacher();
            }

            waitAllUserForStartGame();

        }catch (Exception e) {
            removeAll();
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
            e.printStackTrace();
        }
    }


    //Inizia una partita solo quando il teacher e alcuni studenti sono connessi a questa pagina
    private void waitAllUserForStartGame(){
        //Blocca esecuzione finche' tutti gli studenti (incluso il teacher) non sono connessi a questa pagina
        while(Broadcaster.getListeners().size() <= maxNumeroStutentiConnessi);

        if(account.getTypeAccount().equals("teacher")) {
            if(!isStarted){
                start.click();
            }
        }
    }

    private HorizontalLayout nameUserAndInfoBtnContainer(){
        HorizontalLayout hor1 = new HorizontalLayout();
        hor1.setSpacing(false);
        hor1.setPadding(false);
        hor1.addClassName("nameUserAndInfoBtnContainer");

        String str = "Benvenuta ";
        if(account.getSesso().equals("0")){
            str = "Benvenuto ";
        }

        Label nomeUser = new Label(str + account.getNome());
        nomeUser.getStyle().set("font-size","40px");

        Button b = new Button("Info su Guess");
        b.addClassName("btnInfoGuess");

        DialogUtility dialogUtility = new DialogUtility();
        Dialog d = dialogUtility.descrizioneGiocoDialog(guess);
        b.addClickListener(buttonClickEvent -> {
            d.open();
        });

        hor1.add(nomeUser, b);
        return hor1;
    }



    private void showSelectsFieldGruppiForTeacher(){
        HorizontalLayout hor1 = new HorizontalLayout();
        hor1.addClassName("showSelectsFieldGruppiForTeacherContainer");

        Label lab1 = new Label("Seleziona gruppo: ");
        lab1.getStyle().set("font-size", "16px");
        lab1.getStyle().set("margin-top", "8px");

        Select<String> selects = new Select<>();
        List<String> items = new ArrayList<String>();

        for(Gruppo g : gruppi){
            items.add(g.getId());
        }

        selects.setItems(items);
        if(items.size() > 0)
            selects.setValue(items.get(0));

        selects.addValueChangeListener(event ->{
           String value = event.getValue();
           currentGroupSelect = Utils.findGruppoByName(gruppi, value);

           hideAllContainerPVTeacher();
           Div containerPVteacher = Utils.getDivFromListByAttribute(containersPVTeacher, "name", currentGroupSelect.getId());
           containerPVteacher.getStyle().set("display", "block"); //mostra solo DivContainerPVTeacher in base al currentGroupSelect

           startGameUI.showParolaSuggeritaAndBtnTeacher(currentGroupSelect.getId());
        });

        hor1.add(lab1, selects);
        add(hor1);
    }

    //private methods
    private void hideAllContainerPVTeacher(){
        for(Div i : containersPVTeacher){
            i.getStyle().set("display", "none");
        }
    }

    //public methods
    public static void reset(){
        try {
            Broadcaster.clearPartiteThread();   //interrompi tutti i thread sulle partite e poi fai clear della List
            Broadcaster.getVotes().clear();
            Broadcaster.getAccountList().clear();
            Broadcaster.getItems().clear();
            Broadcaster.getListeners().clear();
            BroadcasterChat.getListeners().clear();
            BroadcasterSuggerisci.getListeners().clear();
            Broadcaster.getParoleVotateHM().clear();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public Image generateImage(Account account) {
        Long id = account.getId();
        StreamResource sr = new StreamResource("user", () ->  {
            Account attached = accountRepository.findWithPropertyPictureAttachedById(id);
            return new ByteArrayInputStream(attached.getProfilePicture());
        });
        sr.setContentType("image/png");
        Image image = new Image(sr, "profile-picture");
        return image;
    }

    //Implementazione metodi della Java interface 'BroadcasterListener'
    @Override
    public void receiveBroadcast(String message) {
        getUI().get().access(() -> {
            String string = message;
            String[] parts = string.split(":");
            String nome = parts[0]+":";
            String testo = parts[1];
            String id = parts[2];
            Account a = accountRepository.findAccountById(Long.parseLong(id));
            Div divmessage = new Div();
            Div nomeP = new Div();
            Label label = new Label(nome);

            nomeP.add(label);
            label.getStyle().set("margin-right","20px");

            Div div = new Div();
            Paragraph paragraph = new Paragraph(testo);
            div.add(paragraph);
            divmessage.addClassName("message");
            if (a.getProfilePicture()!=null){
                image333 = generateImage(a);
                image333.getStyle().set("order","0");
                divmessage.add(image333);
            }else {
                if(a.getSesso()=="1"){
                    image333 = new Image("frontend/img/profiloGirl.png", "foto profilo");
                    image333.getStyle().set("order","0");
                    divmessage.add(image333);
                }
                else {
                    image333 = new Image("frontend/img/profiloBoy.png", "foto profilo");
                    image333.getStyle().set("order","0");
                    divmessage.add(image333);
                }
            }
            divmessage.add(label,div);
            messageList.add(divmessage);
        });
    }

    @Override
    public void countUser(UI ui, String nome) {
        ui.getUI().get().access(() -> {
            numeroUtenti.setEnabled(false);
            numeroUtenti.setText("Utenti connessi: "+Broadcaster.getListeners().size());
            numeroUtenti.setEnabled(true);
        });
    }

    @Override
    public void startGame1(UI ui) {
        boolean flag = false;
        int i = 0;
        try {
            while(!flag){ //finche' la ui non e' attached a this component, cioe' finche' getUI() doesn't contains an UI element
                if(getUI().isPresent()) {
                    getUI().get().accessSynchronously(() -> { //Locks the session of this UI and runs the provided command right away
                        if(account.getTypeAccount().equals("student")){
                            attendiDialog.close();
                        }
                        //StartGameUI startGameUI = new StartGameUI(guessController, isTeacher, account);
                        verticalLayout.add(startGameUI);
                        indizio.getStyle().set("font-size", "30px");
                        indizio.getStyle().set("margin-left", "15px");
                        verticalLayout.add(secondi, indizio);
                        add(verticalLayout);
                    });
                    flag = true;
                }
                i++;
            }

            System.out.println("GuessUI.startGame1() - Account: " + account.getNome() + " CountDelayForGetUI: " + i);
            /*if(i== 100000){  //dopo 100000 iterazioni, la UI non e' stata ottenuta ->  errore
                throw new NoSuchElementException("No value present in getUI()");
            }
             */
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void receiveIndizio(String message) {
        getUI().get().access(() -> {
            Paragraph paragraph = new Paragraph(message);
            paragraph.getStyle().set("font-size","18px");
            paragraph.getStyle().set("margin","5px 0px 0px 15px");
            verticalLayout.add(paragraph);
        });
    }

    @Override
    public void countDown(String n) {
        getUI().get().access(() -> {
            secondi.setEnabled(false);
            secondi.setText("Time: "+ n+" secondi");
            secondi.getStyle().set("font-size","30px");
            secondi.getStyle().set("margin-left","15px");
            secondi.setEnabled(true);
        });
    }

    @Override
    public void parolaVotata(Gruppo gruppo) {   //gruppo da cui proviene la parola inserita

        if(isTeacher){
            parolaVotataTeacher(gruppo);
            return;
        }

        getUI().get().access(() -> {
            Div containerPV = Utils.getDivFromListByAttribute(containersParoleVotate, "name", gruppo.getId());
            containerPV.removeAll();

            Broadcaster.getVotes().forEach((s, integer) -> {
                String a = "";
                if(integer == 1){
                    a = " voto";
                }else
                    a = " voti";

                //Solo ai membri dell'account a verra' mostrato la parola suggerita
                if(Utils.isAccountInThisGruppo(gruppo, account)){
                    Label label = new Label(s + " ha " + integer + a);
                    label.getElement().setAttribute("id", "parolaVotata");
                    MessageList messageList = new MessageList("message-list");
                    messageList.add(label);
                    containerPV.add(messageList);
                    containerPV.getStyle().set("display", "block");
                    add(containerPV);
                }else{  //per gli account che non fanno parte del gruppo, containerPV rimane invisibile
                    containerPV.getStyle().set("display", "none");
                }
            });
        });
    }

    public void parolaVotataTeacher(Gruppo gruppo) {   //gruppo da cui proviene la parola inserita
        getUI().get().access(() -> {
            Div containerPVTeacher = Utils.getDivFromListByAttribute(containersPVTeacher, "name", gruppo.getId());
            containerPVTeacher.removeAll();

            Broadcaster.getVotes().forEach((s, integer) -> {
                String a = "";
                if(integer == 1){
                    a = " voto";
                }else
                    a = " voti";

                Label label = new Label(s + " ha " + integer + a);
                label.getElement().setAttribute("id", "parolaVotata");
                MessageList msgList = new MessageList("message-list");
                msgList.getElement().setAttribute("id", "PV" + gruppo.getId());
                msgList.add(label);
                containerPVTeacher.add(msgList);
                add(containerPVTeacher);
            });
        });
    }

    @Override
    public void partititaVincente(String parola,int punteggio) {
        getUI().get().access(() -> {
            reset();
            removeAll();
            if(account.getTypeAccount().equals("student")) {
                FireWorks fireWorks = new FireWorks();
                add(fireWorks);
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitaVincente(parola, punteggio, guess);
                endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false);
            }else if(account.getTypeAccount().equals("teacher")){
                endGamePublisher.doStuffAndPublishAnEvent("Guess", account, true);
            }
        });

    }

    @Override
    public void partititanonVincente(){
        getUI().get().access(() -> {
            reset();
            removeAll();
            if(account.getTypeAccount().equals("student")) {
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitanonVincente(guess);
                endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false);
            }else if(account.getTypeAccount().equals("teacher")){
                endGamePublisher.doStuffAndPublishAnEvent("Guess", account, true);
            }
        });
    }

    @Override
    public void terminaPartitaFromTeacher() {
        try {
            if(getUI().isPresent()) {   //inserito per evitare exception (No value Present) dovuta al teacher quando effettua il logout e viene invocato questo metodo
                getUI().get().access(() -> {
                    reset();
                    removeAll();
                    if (account.getTypeAccount().equals("student")) {
                        DialogUtility dialogUtility = new DialogUtility();
                        dialogUtility.partitaTerminataFromTeacher();
                        endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false);
                    } else {
                        endGamePublisher.doStuffAndPublishAnEvent("Guess", account, true);
                    }
                });
            }else{
                System.out.println("GuessUI.terminaPartitaFromTeacher() - getUI is not present for Account: " + account.getNome());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //Implements methods of PageConfigurator
    @Override
    public void configurePage(InitialPageSettings initialPageSettings) {
        String script = "window.onbeforeunload = function (e) " +
                "{ var e = e || window.event; document.getElementById(\"GuessUI\").$server.browserIsLeaving(); return; };";
        initialPageSettings.addInlineWithContents(InitialPageSettings.Position.PREPEND, script, InitialPageSettings.WrapMode.JAVASCRIPT);
    }

    @ClientCallable
    public void browserIsLeaving() {

        //Pre-condition
        boolean flag = false;
        for(Account i : Broadcaster.getListeners().keySet()){
            if(i.equals(account)){
                flag = true;
                break;
            }
        }
        if(!flag){  //se il listener non contiene questo 'account' -> non fare nulla
            return;
        }

        System.out.println("GuessUI.browserIsLeaving() e' stato invocato; Account:" + account.getNome());

        if(account.getTypeAccount().equals("teacher")){ //teacher ha effettuato il logout, allora termina per tutti;
            Broadcaster.terminaPartitaFromTeacher();
        }else if(Broadcaster.getListeners().size() > 1) { //se rimuovendo questo utente dal listener, sono presenti almeno 2 account
            Broadcaster.unregister(account, this);
            endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false);
        }else{  //nessun utente e' connesso, quindi termina la partita per tutti gli utenti connessi rimanenti
            Broadcaster.terminaPartitaFromTeacher();
        }
    }

}
