
package com.example.demo.maty.gameMenagement.frondend;


import com.example.demo.entity.Account;
import com.example.demo.entity.Partita;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.games.Maty;
import com.example.demo.gamesRules.Game;
import com.example.demo.maty.gameMenagement.backend.MatyController;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterChatMaty;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterMaty;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterSuggerisciMaty;
import com.example.demo.maty.gameMenagement.backend.db.ItemMaty;
import com.example.demo.maty.gameMenagement.backend.listeners.BroadcastListenerMaty;
import com.example.demo.maty.gameMenagement.backend.listeners.ChatListenerMaty;
import com.example.demo.utility.*;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.Date;

@Push
@Route("maty")
@HtmlImport("chat.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/style.css")
@StyleSheet("frontend://stile/chat.css")
@StyleSheet("frontend://stile/divbox.css")
@StyleSheet("frontend://stile/animation.css")
@JavaScript("frontend://js/script.js")
@PageTitle("ConnecTeam-Maty")
public class MatyUI extends HorizontalLayout implements BroadcastListenerMaty, ChatListenerMaty, PageConfigurator, BeforeEnterObserver {

    //static field
    //Numero di utenti connessi al momento in cui il teacher da' il via alla partita
    private static final int maxNumeroUtentiConnessi = com.example.demo.users.broadcaster.Broadcaster.getNumberOfMatyUser();

    //instance field
    private AccountRepository accountRepository;
    private Account account;
    private MatyController matyController;
    private Label numeroUtenti = new Label();
    private Div containerUtenti = new Div();
    private Image imageU = new Image();
    private VerticalLayout chatMessages = new VerticalLayout();
    private Label secondi = new Label();
    private Label indizio = new Label("Indizi: ");
    private VerticalLayout verticalLayout = new VerticalLayout();
    private Div containerNumeriSS = new Div();
    private Div box = new Div();
    private Div wrapper = new Div();
    private Maty maty;
    private PartitaRepository partitaRepository;
    private Partita partita;
    private MatyController.PartitaThread partitaThread;
    private ItemMaty item;
    boolean isStarted = false;
    private Div chat = new Div();
    MessageList messageList = new MessageList("chatlayoutmessage2");
    HorizontalLayout containerAddendi= new HorizontalLayout();
    private Image image333;
    private boolean isTeacher = false;

    public MatyUI() {

        try {

            setId("MatyUI");
            getStyle().set("height", "100%");

            containerAddendi.addClassName("containerAddendi");
            maty = new Maty();

            //Ottieni valori dalla sessione corrente e verifica se sono presenti in sessione
            account = (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
            if(account == null)
                throw new IllegalArgumentException("MatyUI: Account is null");
            accountRepository = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
            if(accountRepository == null)
                throw new IllegalArgumentException("MatyUI: AccountRepository is null");
            partitaRepository = (PartitaRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("partitaRepository");
            if(partitaRepository == null)
                throw new IllegalArgumentException("MatyUI: PartitaRepository is null");

            matyController = new MatyController(partitaRepository);
            matyController.setAccount(account);
            if(account.getTypeAccount().equals("teacher"))
                isTeacher = true;

            for (int i = 0; i < BroadcasterMaty.getPartiteThread().size(); i++) {
                if (BroadcasterMaty.getPartiteThread().get(i) != null) {
                    isStarted = true;
                }
            }
            if (isStarted != true) {
                BroadcasterMaty.register(account, this);
                BroadcasterChatMaty.register(this);
                BroadcasterMaty.aggiornaUtentiConnessi(UI.getCurrent());
                BroadcasterMaty.addUsers(UI.getCurrent());
            } else {
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEvent("C'è una partita in corso aspetta che finisca", "0");
            }

            if(isTeacher){ //mostra la appbar
                getStyle().set("width", "100%");
                AppBarUI appBar = new AppBarUI("Maty", false); //nome pagina corrente
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
            TextField message1 = new TextField();
            Icon icon = VaadinIcon.PAPERPLANE_O.create();
            icon.setColor("white");
            Button send = new Button(icon);
            message1.addKeyDownListener(Key.ENTER, keyDownEvent -> {
                String mess = message1.getValue();
                if (!mess.equals("")) {
                    BroadcasterChatMaty.broadcast(account.getNome() + ": " + message1.getValue()+":"+account.getId());
                    message1.setValue("");
                }
            });
            message1.getStyle().set("width","80%"); //value precedente: 85%
            message1.getStyle().set("margin-right","16px");
            send.addClickListener(buttonClickEvent -> {
                String mess = message1.getValue();
                if (!mess.equals("")) {
                    BroadcasterChatMaty.broadcast(account.getNome() + ": " + message1.getValue()+":"+account.getId());
                    message1.setValue("");
                }
            });
            send.addClassName("buttonSendChat");
            chat.add(messageList);
            device.add(chat);
            device.add(message1);
            device.add(send);
            add(device);

            //Container che mostra numero di utenti connessi e il pulsante 'Gioca' (Sala di attesa)
            //containerUtenti.addClassName("layoutUsers");
            containerNumeriSS.addClassName("containerNumeriSS");
            box.addClassName("box");

            //Container nome utente e pulsante 'Info' su Guess
            add(nameUserAndInfoBtnContainer());

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
        hor1.getStyle().set("top","6%");  //value precedente: 10%
        hor1.getStyle().set("left","20%");

        String str = "Benvenuta ";
        if(account.getSesso().equals("0")){
            str = "Benvenuto ";
        }

        Label nomeUser = new Label(str + account.getNome());
        nomeUser.getStyle().set("font-size","40px");

        Button b = new Button("Info su Maty");
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

        Label title = new Label("Info su Maty");
        title.getStyle().set("font-size", "32px");
        Label descrizione = new Label(maty.getDescrizioneLungaGioco());
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
    public void countUser(UI ui, String nome) {
        ui.getUI().get().access(() -> {
            numeroUtenti.setEnabled(false);
            numeroUtenti.setText("Utenti connessi: "+BroadcasterMaty.getListeners().size());
            numeroUtenti.setEnabled(true);
        });
    }

    @Override
    public void startGame1() {
        try {
            getUI().get().access(() -> {
                StartGameMatyUI startGameMatyUI = new StartGameMatyUI(matyController,account);
                verticalLayout.add(startGameMatyUI);
                verticalLayout.add(secondi/*,indizio*/);
                indizio.getStyle().set("font-size","30px");
                indizio.getStyle().set("margin-left","15px");
                add(verticalLayout);
            });
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void receiveIndizio(String message) {
        getUI().get().access(() -> {
            if(BroadcasterMaty.getContClick().size() == 5){
                Label label = new Label("Aiuti:");
                verticalLayout.add(label);
                label.getStyle().set("font-size","40px");
                label.getStyle().set("margin-left","15px");
            }
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
            secondi.setText("Tempo: "+ n+" minuti");
            secondi.getStyle().set("font-size","30px");
            secondi.getStyle().set("margin-left","15px");
            secondi.setEnabled(true);
        });
    }

    @Override
    public void addUsers(UI ui, int i) {
        ui.getUI().get().access(() -> {
            containerUtenti.removeAll();
            BroadcasterMaty.getListeners().forEach((account1, broadcastListener) -> {
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
    }

    @Override
    public void numeroInserito(String operazione) {
        getUI().get().access(() -> {
            int j;
            containerNumeriSS.removeAll();
            if (operazione.equals("somma")){
                j=0;
            }else {
                j =1;
            }
            for (int  i=j; i<BroadcasterMaty.getIntegers().size(); i++) {

                box.removeAll();
                wrapper.removeAll();

                Div circle = new Div();
                circle.addClassName("circle");
                circle.setId("colorpad1");
                Paragraph paragraph = new Paragraph();
                paragraph.addClassName("parag1");
                Span span = new Span(""+BroadcasterMaty.getIntegers().get(i));
                paragraph.add(span);
                circle.add(paragraph);
                box.add(circle);
                add(box);

                Div d = new Div();
                d.setWidth(null);
                Paragraph p = new Paragraph(""+BroadcasterMaty.getIntegers().get(i));
                p.addClassName("parag2");
                d.addClassName("paer");
                d.setId("colorpad");
                d.add(p);

                wrapper.add(d);
                wrapper.addClassName("box1");
                add(wrapper);
                getElement().executeJavaScript("setRandomColor()");
            }

        });
    }//non usato

    @Override
    public void partititaVincente(String parola, int punteggio) {
        Game game1 = maty;
        getUI().get().access(() -> {
            reset();
            removeAll();
            FireWorks fireWorks = new FireWorks();
            add(fireWorks);
            DialogUtility dialogUtility = new DialogUtility();
            dialogUtility.partitaVincente(parola,punteggio,game1);
        });
    }

    @Override
    public void partititanonVincente() {
        Game game1 = maty;
        getUI().get().access(() -> {
            reset();
            removeAll();
            DialogUtility dialogUtility = new DialogUtility();
            dialogUtility.partitanonVincente(game1);
        });
    }

    Label labelNum = new Label();
    @Override
    public void numeroDaSotrarre(String numero,String numOriginzle) {
        getUI().get().access(() -> {
            remove(labelNum);
            labelNum.setText("Raggiungi "+numOriginzle+" sottraendo " +BroadcasterMaty.getListeners().size()+" sottraendi a "+numero +"!.");
            labelNum.addClassName("labelNumSottrarre");
            add(labelNum);
        });
    }

    @Override
    public void numeroDaSommare(String numOriginzle) {
        getUI().get().access(() -> {
            remove(labelNum);
            labelNum.setText("Raggiungi "+numOriginzle+" sommando " +BroadcasterMaty.getListeners().size()+" addendi!.");
            labelNum.addClassName("labelNumSottrarre");
            add(labelNum);
        });
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
    public void configurePage(InitialPageSettings initialPageSettings) {
        BroadcasterMaty.getListeners().forEach((account1, broadcastListenerMaty) -> {
            if (account.equals(account1)) {
                String script = "window.onbeforeunload = function (e) " +
                        "{ var e = e || window.event; document.getElementById(\"MatyUI\").$server.browserIsLeaving(); return; };";
                initialPageSettings.addInlineWithContents(InitialPageSettings.Position.PREPEND, script, InitialPageSettings.WrapMode.JAVASCRIPT);
            }
        });
    }

    static public void reset(){

        try {
            BroadcasterMaty.getIntegers().clear();
            BroadcasterMaty.getPartiteThread().clear();
            BroadcasterSuggerisciMaty.getItems().clear();
            BroadcasterMaty.getAccountList().clear();
            BroadcasterMaty.getItems().clear();
            BroadcasterMaty.getListeners().clear();
            BroadcasterChatMaty.getListeners().clear();
            BroadcasterSuggerisciMaty.getListeners().clear();
            BroadcasterMaty.getContClick().clear();
        }catch (Exception e){
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

    @ClientCallable
    public void browserIsLeaving() {
        BroadcasterMaty.getListeners().forEach((account1, broadcastListener) -> {
            System.out.println("Account registrato alla partita = "+account1.getNome());
        });
        try {
            BroadcasterMaty.getListeners().forEach((account1, broadcastListener) -> {
                if (account1.equals(account)) {
                    BroadcasterMaty.browserIsLeavingCalled(account);
                    BroadcasterMaty.unregister(account, this);
                    for (int i = 0; i < BroadcasterMaty.getPartiteThread().size(); i++) {
                        try {
                            BroadcasterMaty.getPartiteThread().get(i).interrupt();
                            BroadcasterMaty.getPartiteThread().get(i).stopTimer();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    reset();
                }
            });
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        System.out.println("Maty: #account: " + BroadcasterMaty.getListeners().size() + "- #Max account: " + maxNumeroUtentiConnessi);
        if(isStarted != true && BroadcasterMaty.getListeners().size() == maxNumeroUtentiConnessi) {
            System.out.println("MatyUI: Partita iniziata!");
            for (int i = 0; i < BroadcasterMaty.getPartiteThread().size(); i++) {
                if (BroadcasterMaty.getPartiteThread().get(i) != null) {
                    isStarted = true;
                }
            }
            if (isStarted != true) {
                partita = new Partita(new Timestamp(new Date().getTime()), "Maty");
                matyController.startGame(partita);
                partitaThread = matyController.getPartitaThread();
                item = matyController.getItem();
                BroadcasterMaty.startGame(partitaThread, item);
            } else {
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEvent("C'è una partita in corso aspetta che finisca", "10");
            }
        }
    }
}