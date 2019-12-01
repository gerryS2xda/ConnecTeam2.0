
package com.example.demo.maty.gameMenagement.frondend;


import com.example.demo.entity.Account;
import com.example.demo.entity.Partita;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.games.Maty;
import com.example.demo.maty.gameMenagement.backend.MatyController;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterChatMaty;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterMaty;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterSuggerisciMaty;
import com.example.demo.maty.gameMenagement.backend.db.ItemMaty;
import com.example.demo.maty.gameMenagement.backend.listeners.BroadcastListenerMaty;
import com.example.demo.maty.gameMenagement.backend.listeners.ChatListenerMaty;
import com.example.demo.users.event.EndGameEventBeanPublisher;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.*;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MatyUI extends HorizontalLayout implements BroadcastListenerMaty, ChatListenerMaty, PageConfigurator {


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
    private boolean isStarted = false;
    private Div chat = new Div();
    private MessageList messageList = new MessageList("chatlayoutmessage2");
    private HorizontalLayout containerAddendi= new HorizontalLayout();
    private Image image333;
    private boolean isTeacher = false;
    private Label labelNum = new Label();
    private EndGameEventBeanPublisher endGamePublisher;
    //Numero di utenti connessi al momento in cui il teacher da' il via alla partita
    private int maxNumeroStutentiConnessi = 0;
    private WrappedSession teacherSession;
    private Button start; //pulsante che sara' invisibile
    private Dialog attendiDialog;

    public MatyUI(@Autowired EndGameEventBeanPublisher endGameEventBeanPublisher) {

        try {
            //Inizializzazione
            setId("MatyUI");
            getStyle().set("height", "100%");
            maty = new Maty();
            endGamePublisher = endGameEventBeanPublisher;
            maxNumeroStutentiConnessi = com.example.demo.users.broadcaster.Broadcaster.getNumberOfMatyUser();
            teacherSession = com.example.demo.users.broadcaster.Broadcaster.getTeacherSession();

            containerAddendi.addClassName("containerAddendi");

            if(VaadinService.getCurrentRequest() != null) {
                //Ottieni valori dalla sessione corrente e verifica se sono presenti in sessione
                account = (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
                if (account == null)
                    throw new IllegalArgumentException("MatyUI: Account is null");
                accountRepository = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
                if (accountRepository == null)
                    throw new IllegalArgumentException("MatyUI: AccountRepository is null");
                partitaRepository = (PartitaRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("partitaRepository");
                if (partitaRepository == null)
                    throw new IllegalArgumentException("MatyUI: PartitaRepository is null");
            }else{ //getCurrentRequest() is null (poiche' e' il server che 'impone' accesso a questa pagina - no memorizzazione stato partita)
                account = (Account) teacherSession.getAttribute("user");
                accountRepository = (AccountRepository) teacherSession.getAttribute("rep");
                partitaRepository = (PartitaRepository) teacherSession.getAttribute("partitaRepository");
            }

            matyController = new MatyController(partitaRepository);

            if(account.getTypeAccount().equals("teacher")) {
                isTeacher = true;
                UI.getCurrent().setPollInterval(1000);
            }else{
                DialogUtility dialogUtility = new DialogUtility();
                attendiDialog = dialogUtility.showDialog("Attendere...", "black");
                attendiDialog.open();
            }

            //Per ogni partita gia' iniziata, setta isStarted a true (una sola partita alla volta)
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
                AppBarUI appBar = new AppBarUI("Maty", false, true); //nome pagina corrente
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
                        BroadcasterChatMaty.broadcast("Teacher: " + message1.getValue()+":"+account.getId());
                    else
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
            textFieldSendBtn.add(message1, send);

            chat.add(messageList);
            device.add(chat);
            device.add(textFieldSendBtn);
            add(device);

            //Container che mostra numero di utenti connessi e il pulsante 'Gioca' (Sala di attesa)
            //containerUtenti.addClassName("layoutUsers");
            containerNumeriSS.addClassName("containerNumeriSS");
            box.addClassName("box");

            //Container nome utente e pulsante 'Info' su Maty
            if(!isTeacher) {
                add(nameUserAndInfoBtnContainer());
            }

            //Implementazione di un pulsante invisibile 'start' che verra' 'cliccato' dal teacher
            start = new Button();
            start.getStyle().set("display", "none");
            start.addClickListener(buttonClickEvent -> {
                System.out.println("Maty: Partita iniziata!");
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
            });
            add(start);
            waitAllUserForStartGame();
        }
        catch (Exception e) {
            removeAll();
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
            e.printStackTrace();
        }
    }

    //Inizia una partita solo quando il teacher e alcuni studenti sono connessi a questa pagina
    private void waitAllUserForStartGame(){
        System.out.println("waitAllUserForStartGame(): Listener: " + BroadcasterMaty.getListeners().size() + " maxNumeroStutentiConnessi: " + maxNumeroStutentiConnessi);

        //Blocca esecuzione finche' tutti gli studenti (incluso il teacher) non sono connessi a questa pagina
        while(BroadcasterMaty.getListeners().size() <= maxNumeroStutentiConnessi);

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

        DialogUtility dialogUtility = new DialogUtility();
        Dialog d = dialogUtility.descrizioneGiocoDialog(maty);
        b.addClickListener(buttonClickEvent -> {
            d.open();
        });

        hor1.add(nomeUser, b);
        return hor1;
    }

    //public methods
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

    public static void reset(){
        try {
            BroadcasterMaty.getIntegers().clear();
            BroadcasterMaty.clearPartiteThread();   //interrompi tutti i thread sulle partite e poi fai clear della List
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
        boolean flag = false;
        int i = 0;
        try {
            while(!flag) { //finche' la ui non e' attached a this component, cioe' finche' getUI() doesn't contains an UI element
                if (getUI().isPresent()) {
                    getUI().get().accessSynchronously(() -> { //Locks the session of this UI and runs the provided command right away
                        if(account.getTypeAccount().equals("student")){
                            attendiDialog.close();
                        }
                        StartGameMatyUI startGameMatyUI = new StartGameMatyUI(matyController, account, isTeacher);
                        verticalLayout.add(startGameMatyUI);
                        verticalLayout.add(secondi/*,indizio*/);
                        indizio.getStyle().set("font-size", "30px");
                        indizio.getStyle().set("margin-left", "15px");
                        add(verticalLayout);
                    });
                    flag = true;
                }
                i++;
            }
            System.out.println("MatyUI.startGame1() - Account: " + account.getNome() + " CountDelayForGetUI: " + i);
        }catch (Exception e){
            e.printStackTrace();
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
                getElement().executeJs("setRandomColor()");
            }

        });
    }//non usato

    @Override
    public void partititaVincente(String parola, int punteggio) {
        getUI().get().access(() -> {
            reset();
            removeAll();
            if(account.getTypeAccount().equals("student")) {
                FireWorks fireWorks = new FireWorks();
                add(fireWorks);
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitaVincente(parola, punteggio, maty);
                endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, false);
            }else if(account.getTypeAccount().equals("teacher")){
                endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, true);
            }
        });
    }

    @Override
    public void partititanonVincente() {
        getUI().get().access(() -> {
            reset();
            removeAll();
            if(account.getTypeAccount().equals("student")) {
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitanonVincente(maty);
                endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, false);
            }else if(account.getTypeAccount().equals("teacher")){
                endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, true);
            }
        });
    }

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
    public void terminaPartitaFromTeacher() {
        try {
            getUI().get().access(() -> {
                reset();
                removeAll();
                if(account.getTypeAccount().equals("student")) {
                    DialogUtility dialogUtility = new DialogUtility();
                    dialogUtility.partitaTerminataFromTeacher();
                    endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, false);
                }else{
                    endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, true);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //Implements methods of PageConfigurator
    @Override
    public void configurePage(InitialPageSettings initialPageSettings) {
        String script = "window.onbeforeunload = function (e) " +
                "{ var e = e || window.event; document.getElementById(\"MatyUI\").$server.browserIsLeaving(); return; };";
        initialPageSettings.addInlineWithContents(InitialPageSettings.Position.PREPEND, script, InitialPageSettings.WrapMode.JAVASCRIPT);
    }

    @ClientCallable
    public void browserIsLeaving() {

        //Pre-condition
        boolean flag = false;
        for(Account i : BroadcasterMaty.getListeners().keySet()){
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
            BroadcasterMaty.terminaPartitaFromTeacher();
        }else if(BroadcasterMaty.getListeners().size() > 1) { //se rimuovendo questo utente dal listener, sono presenti almeno 2 account
            BroadcasterMaty.unregister(account, this);
            endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false);
        }else{  //nessun utente e' connesso, quindi termina la partita per tutti gli utenti connessi rimanenti
            BroadcasterMaty.terminaPartitaFromTeacher();
        }
    }

}