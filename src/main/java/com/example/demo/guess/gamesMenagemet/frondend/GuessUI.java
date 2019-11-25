package com.example.demo.guess.gamesMenagemet.frondend;

import com.example.demo.entity.Account;
import com.example.demo.entity.Partita;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.gamesRules.Game;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.Date;

@Push
@Route("guess")
@HtmlImport("chat.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/style.css")
@StyleSheet("frontend://stile/chat.css")
@PageTitle("ConnecTeam-Guess")
public class GuessUI extends HorizontalLayout implements BroadcastListener, ChatListener, BeforeLeaveObserver, BeforeEnterObserver {

    //static field
    //Numero di utenti connessi al momento in cui il teacher da' il via alla partita
    private static final int maxNumeroStutentiConnessi = com.example.demo.users.broadcaster.Broadcaster.getNumberOfGuessUser();
    private static WrappedSession teacherSession = com.example.demo.users.broadcaster.Broadcaster.getTeacherSession();

    //instance field
    private AccountRepository accountRepository;
    private Account account;
    private GuessController guessController;
    private Label numeroUtenti = new Label();
    private Div containerUtenti = new Div();
    private Image imageU = new Image();
    private VerticalLayout chatMessages = new VerticalLayout();
    private Label secondi = new Label();
    private Label indizio = new Label("Indizi: ");
    private VerticalLayout verticalLayout = new VerticalLayout();
    private Div containerParoleVotate = new Div();
    private Guess guess;
    private PartitaRepository partitaRepository;
    private Partita partita;
    private GuessController.PartitaThread partitaThread;
    private Item item;
    boolean isStarted = false;
    private Div chat = new Div();
    MessageList messageList = new MessageList("chatlayoutmessage2");
    private Image image333;
    private boolean isTeacher = false;
    private EndGameEventBeanPublisher endGamePublisher;

    public GuessUI(@Autowired EndGameEventBeanPublisher endGameEventBeanPublisher) {

        try {
            setId("GuessUI");
            getStyle().set("height", "100%");
            guess = new Guess();
            endGamePublisher = endGameEventBeanPublisher;

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
                UI.getCurrent().setPollInterval(1000);
            }
            guessController = new GuessController(partitaRepository);
            guessController.setAccount(account); //invia account attuale a GuessController (serve per endgameEvent)

            if(account.getTypeAccount().equals("teacher"))
                isTeacher = true;

            for(Account i : Broadcaster.getPartiteThread().keySet()){
                if(Broadcaster.getPartiteThread().get(i) != null){
                    isStarted = true;
                }
            }

            if (isStarted != true) {
                Broadcaster.register(account, this);
                BroadcasterChat.register(this);
                Broadcaster.aggiornaUtentiConnessi(UI.getCurrent());
                Broadcaster.addUsers(UI.getCurrent());
            } else {
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
                    BroadcasterChat.broadcast(account.getNome() + ": " + message1.getValue()+":"+account.getId());
                    message1.setValue("");
                }
            });
            message1.getStyle().set("width","80%"); //valore precedente: 85%
            message1.getStyle().set("margin-right","16px");
            send.addClickListener(buttonClickEvent -> {
                String mess = message1.getValue();
                if (!mess.equals("")) {
                    BroadcasterChat.broadcast(account.getNome() + ": " + message1.getValue()+":"+account.getId());
                    message1.setValue("");
                }
            });
            send.addClassName("buttonSendChat");
            textFieldSendBtn.add(message1, send);

            chat.add(messageList);
            device.add(chat);
            device.add(textFieldSendBtn);
            add(device);

            containerUtenti.addClassName("layoutUsers");
            containerParoleVotate.addClassName("containerParoleVotate");
            if(isTeacher){
                containerParoleVotate.getStyle().set("width", "15%");
                containerParoleVotate.getStyle().set("top", "30%");
                containerParoleVotate.getStyle().set("height", "150px");
            }

            //Container nome utente e pulsante 'Info' su Guess
            if(!isTeacher) {
                add(nameUserAndInfoBtnContainer());
            }

        }

        catch (Exception e) {
            removeAll();
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
            e.printStackTrace();
        }
    }

    private HorizontalLayout nameUserAndInfoBtnContainer(){
        HorizontalLayout hor1 = new HorizontalLayout();
        hor1.setSpacing(false);
        hor1.setPadding(false);
        hor1.getStyle().set("position","absolute");
        hor1.getStyle().set("top","10%");
        hor1.getStyle().set("left","20%");

        String str = "Benvenuta ";
        if(account.getSesso().equals("0")){
            str = "Benvenuto ";
        }

        Label nomeUser = new Label(str + account.getNome());
        nomeUser.getStyle().set("font-size","40px");

        Button b = new Button("Info su Guess");
        b.getStyle().set("background-color","#007d99");
        b.getStyle().set("margin-top","16px");
        b.getStyle().set("margin-left","36px");
        b.getStyle().set("cursor","pointer");
        b.getStyle().set("color","white");
        Dialog d = descrizioneGiocoDialog();
        b.addClickListener(buttonClickEvent -> {
            d.open();
        });

        hor1.add(nomeUser, b);
        return hor1;
    }

    private Dialog descrizioneGiocoDialog(){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);
        d.setWidth("640px");
        d.setHeight("320px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("height", "100%");

        Label title = new Label("Info su Guess");
        title.getStyle().set("font-size", "32px");
        Label descrizione = new Label(guess.getDescrizioneLungaGioco());
        descrizione.getStyle().set("font-size", "16px");
        Button cancelButton = new Button("Close");
        cancelButton.getStyle().set("background-color","#007d99");
        cancelButton.getStyle().set("cursor","pointer");
        cancelButton.getStyle().set("color","white");
        cancelButton.getStyle().set("margin-top", "50px");
        cancelButton.addClickListener(buttonClickEvent -> {
            d.close();
        });
        content.add(title, descrizione, cancelButton);

        d.add(content);
        return d;
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
        try {
            getUI().get().access(() -> {
                StartGameUI startGameUI = new StartGameUI(guessController, isTeacher);
                verticalLayout.add(startGameUI);
                indizio.getStyle().set("font-size","30px");
                indizio.getStyle().set("margin-left","15px");
                verticalLayout.add(secondi,indizio);
                add(verticalLayout);
            });
        }catch (Exception e){
            System.out.println(e.getMessage());
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
    public void addUsers(UI ui, int in) {
        ui.getUI().get().access(() -> {
            containerUtenti.removeAll();
            Broadcaster.getListeners().forEach((account1, broadcastListener) -> {

                if(account1.getProfilePicture() != null){
                    imageU = generateImage(account1);
                    imageU.getStyle().set("width","50px");
                    imageU.getStyle().set("height","50px");
                    imageU.getStyle().set("border-radius","80px");

                }else {
                    if(account1.getSesso()=="1"){
                        imageU = new Image("frontend/img/profiloGirl.png", "foto profilo");
                        imageU.getStyle().set("width","50px");
                        imageU.getStyle().set("height","50px");
                        imageU.getStyle().set("border-radius","80px");

                    }
                    else {
                        imageU = new Image("frontend/img/profiloBoy.png", "foto profilo");
                        imageU.getStyle().set("width","50px");
                        imageU.getStyle().set("height","50px");
                        imageU.getStyle().set("border-radius","80px");

                    }
                }

                System.out.println("Account id  = "+account1.getId());

                Button button = new Button(imageU);
                button.addClassName("buttonUserGuess");
                button.setText(account1.getNome());
                button.setEnabled(false);
                MessageList messageList = new MessageList("message-list");
                messageList.add(button);
                containerUtenti.add(messageList);

            });
        });
        //getUI().get().getPage().reload();
    }

    @Override
    public void parolaVotata() {
        getUI().get().access(() -> {
            containerParoleVotate.removeAll();
            Broadcaster.getVotes().forEach((s, integer) -> {
                String a = "";
                if(integer == 1){
                    a = " voto";
                }else
                    a = " voti";

                Label label = new Label(s +" ha "+ integer + a );
                MessageList messageList = new MessageList("message-list");
                messageList.add(label);
                containerParoleVotate.add(messageList);
                add(containerParoleVotate);
            });

        });
    }

    @Override
    public void partititaVincente(String parola,int punteggio) {
        Game game = guess;
        getUI().get().access(() -> {
            reset();
            removeAll();
            FireWorks fireWorks = new FireWorks();
            add(fireWorks);
            DialogUtility dialogUtility = new DialogUtility();
            dialogUtility.partitaVincente(parola,punteggio,game);
        });

    }

    @Override
    public void partititanonVincente(){
        Game game = guess;
        getUI().get().access(() -> {
            reset();
            removeAll();
            DialogUtility dialogUtility = new DialogUtility();
            dialogUtility.partitanonVincente(game);
        });
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

    static public void reset(){
        Broadcaster.getPartiteThread().clear();
        Broadcaster.getVotes().clear();
        Broadcaster.getAccountList().clear();
        Broadcaster.getItems().clear();
        Broadcaster.getListeners().clear();
        BroadcasterChat.getListeners().clear();
        BroadcasterSuggerisci.getListeners().clear();
        Broadcaster.getStrings().clear();
    }

    //Implement methods of BeforeLeaveObserver interface
    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        System.out.println("GuessUI.beforeLeave() e' stato invocato");

        if(Broadcaster.getListeners().size() > 1) {
            Broadcaster.unregister(account, this);
            Broadcaster.removePartitaThread(account);
            endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false);
        }else{  //nessun utente e' connesso, quindi termina la partita per tutti gli utenti connessi rimanenti
            Broadcaster.unregister(account, this);
            Broadcaster.removePartitaThread(account);
            showDialogFinePartitaNoTeacher();
            endGamePublisher.doStuffAndPublishAnEvent("Guess", account, true);
            reset();
        }
    }

    private void showDialogFinePartitaNoTeacher(){
        try {
            getUI().get().access(() -> {
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitaTerminataConMotivazione("Partita Terminata! Non risultano altri utenti connessi");
            });
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void browserIsLeavingCalled(Account account) {
        try {
            getUI().get().access(() -> {
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitaTerminata(account);
            });
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        System.out.println("GuessUI: #account: " + Broadcaster.getListeners().size() + "- #Max account: " + maxNumeroStutentiConnessi);
        if(isStarted != true && Broadcaster.getListeners().size() > maxNumeroStutentiConnessi) { //includi anche il teacher (solo studenti era ==)
            System.out.println("GuessUI: Partita iniziata!");
            for(Account i : Broadcaster.getPartiteThread().keySet()){
                if(Broadcaster.getPartiteThread().get(i) != null){
                    isStarted = true;
                }
            }
            if (isStarted != true) {
                partita = new Partita(new Timestamp(new Date().getTime()), "Guess");
                guessController.startGame(partita);
                partitaThread = guessController.getPartitaThread();
                item = guessController.getItem();
                Broadcaster.startGame(UI.getCurrent(), account, partitaThread, item);
            } else {
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEvent("C'è una partita in corso aspetta che finisca", "10");
            }
        }
    }
}
