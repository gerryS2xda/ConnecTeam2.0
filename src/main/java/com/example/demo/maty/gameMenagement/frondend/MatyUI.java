
package com.example.demo.maty.gameMenagement.frondend;


import com.example.demo.chat.BroadcasterChat;
import com.example.demo.chat.ChatListener;
import com.example.demo.chat.ChatUI;
import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.entity.Partita;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.games.Maty;
import com.example.demo.maty.gameMenagement.backend.MatyController;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterMaty;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterSuggerisciMaty;
import com.example.demo.maty.gameMenagement.backend.db.ItemMaty;
import com.example.demo.maty.gameMenagement.backend.listeners.BroadcastListenerMaty;
import com.example.demo.userOperation.NavBar;
import com.example.demo.users.event.EndGameEventBeanPublisher;
import com.example.demo.utility.*;
import com.vaadin.flow.component.ClientCallable;
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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Push
@Route("maty")
@HtmlImport("chat.html")
@StyleSheet("frontend://stile/stile.css")
//@StyleSheet("frontend://stile/style.css")
@StyleSheet("frontend://stile/chat.css")
@StyleSheet("frontend://stile/divbox.css")
@StyleSheet("frontend://stile/animation.css")
@StyleSheet("frontend://stile/matyStyle.css")
@JavaScript("frontend://js/script.js")
@PageTitle("ConnecTeam-Maty")
public class MatyUI extends HorizontalLayout implements BroadcastListenerMaty, ChatListener, PageConfigurator {

    //static field
    public static Gruppo currentGroupSelect;

    //instance field
    private AccountRepository accountRepository;
    private Account account;
    private MatyController matyController;
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
    private HorizontalLayout containerAddendi= new HorizontalLayout();
    private boolean isTeacher = false;
    private Label labelNum = new Label();
    private EndGameEventBeanPublisher endGamePublisher;
    //Numero di utenti connessi al momento in cui il teacher da' il via alla partita
    private int maxNumeroStutentiConnessi = 0;
    private AppBarUI appBarUI;
    private WrappedSession teacherSession;
    private StartGameMatyUI startGameMatyUI; //Aggiungi controllo 'null' prima di usare
    private Button start; //pulsante che sara' invisibile
    private Dialog attendiDialog;
    private List<Gruppo> gruppi = new ArrayList<Gruppo>();
    private NavBar navBar;
    private Dialog chatContainerDialog;
    private ChatUI chatUI;


    public MatyUI(@Autowired EndGameEventBeanPublisher endGameEventBeanPublisher) {

        try {
            //Inizializzazione
            setId("MatyUI");
            getStyle().set("height", "100%");
            maty = new Maty();
            endGamePublisher = endGameEventBeanPublisher;
            maxNumeroStutentiConnessi = com.example.demo.users.broadcaster.Broadcaster.getNumberOfMatyUser();
            teacherSession = com.example.demo.users.broadcaster.Broadcaster.getTeacherSession();
            gruppi = com.example.demo.users.broadcaster.Broadcaster.getGruppiListReceive();
            currentGroupSelect = new Gruppo();
            currentGroupSelect.setId("Gruppo 1"); //per default viene selezionato il 'Gruppo 1'

            UI.getCurrent().getElement().getStyle().set("overflow", "hidden"); //accedi al <body> element

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

            if(account.getTypeAccount().equals("teacher")) {
                isTeacher = true;
                //UI.getCurrent().setPollInterval(1000); //Per il teacher: Da usare solo le pagina viene caricata con UI.navigate(...)
            }else{
                DialogUtility dialogUtility = new DialogUtility();
                attendiDialog = dialogUtility.showDialog("Attendere...", "black");
                attendiDialog.open();
            }

            matyController = new MatyController(partitaRepository);

            //Per ogni partita gia' iniziata, setta isStarted a true (una sola partita alla volta)
            for (int i = 0; i < BroadcasterMaty.getPartiteThread().size(); i++) {
                if (BroadcasterMaty.getPartiteThread().get(i) != null) {
                    isStarted = true;
                }
            }

            if (isStarted != true) {
                BroadcasterMaty.register(account, this);
                BroadcasterChat.register(account, this);
            } else {
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEvent("C'è una partita in corso aspetta che finisca", "0");
            }

            if(isTeacher){ //mostra la appbar
                getStyle().set("width", "100%");
                appBarUI = new AppBarUI("Maty", false, true); //nome pagina corrente
                add(appBarUI);
                showButtonInAppBar();
            }else{
                //mostra la navBar orizzontale se e' stutente
                navBar = new NavBar(true);
                navBar.addClassName("navBarHorizontal");
                add(navBar);
                addChatBtnInNavBar();
            }

            //Chat container
            chatUI = new ChatUI(new Maty(), account, accountRepository, gruppi);
            chatContainerDialog = createDialogWithChatContent();

            //Container che mostra numero di utenti connessi e il pulsante 'Gioca' (Sala di attesa)
            //containerUtenti.addClassName("layoutUsers");
            containerNumeriSS.addClassName("containerNumeriSS");
            box.addClassName("box");

            //Container nome utente e pulsante 'Info' su Maty
            if(!isTeacher) {
                add(nameUserAndInfoBtnContainer());
            }else{
                showSelectsFieldGruppiForTeacher();
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

        Image logoMaty = new Image("frontend/img/Maty.jpeg", "maty");
        DialogUtility dialogUtility = new DialogUtility();
        Dialog d = dialogUtility.descrizioneGiocoDialog(maty, logoMaty);
        b.addClickListener(buttonClickEvent -> {
            d.open();
        });

        hor1.add(nomeUser, b);
        return hor1;
    }

    private void showButtonInAppBar(){
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.getStyle().set("position", "absolute");
        horizontalLayout.getStyle().set("left", "80%");
        horizontalLayout.getStyle().set("z-index", "2");
        horizontalLayout.setHeight(AppBarUI.APPBAR_HEIGHT);

        DialogUtility dialogUtility = new DialogUtility();

        Icon info = new Icon(VaadinIcon.INFO_CIRCLE_O);
        info.setSize(AppBarUI.ICON_BTN_SIZE);
        Button infoBtn = new Button("Info", info);
        infoBtn.setHeight(AppBarUI.APPBAR_HEIGHT);
        infoBtn.getStyle().set("background-color", "#0000");
        infoBtn.getStyle().set("margin", "0");
        infoBtn.addClickListener(buttonClickEvent -> {
            Image logoMaty = new Image("frontend/img/Maty.jpeg", "maty");
            dialogUtility.descrizioneGiocoDialog(new Maty(), logoMaty).open();
        });

        Icon chat = new Icon(VaadinIcon.CHAT);
        chat.setSize(AppBarUI.ICON_BTN_SIZE);
        Button chatBtn = new Button("Chat", chat);
        chatBtn.setHeight(AppBarUI.APPBAR_HEIGHT);
        chatBtn.getStyle().set("background-color", "#0000");
        chatBtn.getStyle().set("margin", "0");
        chatBtn.addClickListener(buttonClickEvent -> {
            chatContainerDialog.open();
        });

        Icon close = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
        close.setSize(AppBarUI.ICON_BTN_SIZE);
        Button terminateGame = new Button("Termina partita", close);
        terminateGame.setHeight(AppBarUI.APPBAR_HEIGHT);
        terminateGame.getStyle().set("background-color", "#0000");
        terminateGame.getStyle().set("margin", "0");
        terminateGame.addClickListener(buttonClickEvent -> {
            BroadcasterMaty.terminaPartitaFromTeacher();
            com.example.demo.users.broadcaster.Broadcaster.setCountMatyUser(0); //reset counter giocatori di Maty
        });

        horizontalLayout.add(infoBtn, chatBtn, terminateGame);
        appBarUI.add(horizontalLayout);
    }

    private void addChatBtnInNavBar(){
        HorizontalLayout chatContainer = new HorizontalLayout();
        chatContainer.getElement().setAttribute("id", "chatContainerNVMatyUI");
        chatContainer.getStyle().set("position", "absolute");
        chatContainer.getStyle().set("left", "80%");

        Icon chatIcon = new Icon(VaadinIcon.CHAT);
        chatIcon.setSize("30px");
        chatIcon.setColor("#007d99");
        chatIcon.getStyle().set("margin-top", "6px");
        chatIcon.getStyle().set("margin-right", "8px");

        Button chatBtn = new Button("Chat");
        chatBtn.addClassName("buttonChatStyleNavBar");
        chatBtn.addClickListener(buttonClickEvent -> {
            chatContainerDialog.open();
        });

        chatContainer.add(chatIcon, chatBtn);
        navBar.getChatContainer().add(chatContainer);
    }

    private Dialog createDialogWithChatContent(){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setWidth("100%");
        d.setHeight("100%");

        d.add(chatUI);

        return d;
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

        selects.addValueChangeListener(event -> {
            String value = event.getValue();
            currentGroupSelect = Utils.findGruppoByName(gruppi, value);

            if(startGameMatyUI == null){
                throw new IllegalArgumentException("StartGameMatyUI is null!!");
            }

            String groupId = currentGroupSelect.getId();

            startGameMatyUI.hideAllContainerForTeacher();
            Div containerBox = Utils.getDivFromListByAttribute(startGameMatyUI.getContainersBoxTeacher(), "name", groupId);
            containerBox.getStyle().set("display", "block");

            VerticalLayout parolaLayout = Utils.getVerticalLayoutFromListByAttribute(startGameMatyUI.getParolaLayoutTeacherList(), "name", groupId);
            parolaLayout.getStyle().set("display", "flex");

            VerticalLayout cronologiaNumeri = Utils.getVerticalLayoutFromListByAttribute(startGameMatyUI.getCronologiaNUmeriTeacherList(), "name", groupId);
            cronologiaNumeri.getStyle().set("display", "flex");

            chatUI.hideAllSpazioMessaggiTeacher();
            MessageList spaziomsgTeacher = Utils.getMessageListFromListByAttributeForChat(chatUI.getSpazioMessaggiTeacher(), "name", groupId);
            spaziomsgTeacher.getStyle().set("display", "block"); //mostra solo DivContainerPVTeacher in base al currentGroupSelect

        });

        hor1.add(lab1, selects);
        add(hor1);
    }

    //public methods
    public static void reset(){
        try {
            BroadcasterMaty.getIntegers().clear();
            BroadcasterMaty.clearPartiteThread();   //interrompi tutti i thread sulle partite e poi fai clear della List
            BroadcasterSuggerisciMaty.getItems().clear();
            BroadcasterMaty.getAccountList().clear();
            BroadcasterMaty.getItems().clear();
            BroadcasterMaty.getListeners().clear();
            BroadcasterChat.getListeners().clear();
            BroadcasterSuggerisciMaty.getListeners().clear();
            BroadcasterMaty.getContClick().clear();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Implementazione metodi della Java interface 'BroadcasterListener'
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
                        startGameMatyUI = new StartGameMatyUI(matyController, account, isTeacher);
                        verticalLayout.add(startGameMatyUI);
                        verticalLayout.add(secondi);
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
            if(chatContainerDialog.isOpened())
                chatContainerDialog.close();
            reset();
            removeAll();
            if(account.getTypeAccount().equals("student")) {
                FireWorks fireWorks = new FireWorks();
                add(fireWorks);
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitaVincente(parola, punteggio, maty);
                endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, false, new Gruppo(), "");
            }else if(account.getTypeAccount().equals("teacher")){
                endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, true, new Gruppo(), "");
            }
        });
    }

    @Override
    public void partititanonVincente() {
        getUI().get().access(() -> {
            if(chatContainerDialog.isOpened())
                chatContainerDialog.close();
            reset();
            removeAll();
            if(account.getTypeAccount().equals("student")) {
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitanonVincente(maty);
                endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, false, new Gruppo(), "");
            }else if(account.getTypeAccount().equals("teacher")){
                endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, true, new Gruppo(), "");
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
    public void terminaPartitaFromTeacher() {
        try {
            if(getUI().isPresent()) {   //inserito per evitare exception (No value Present) dovuta al teacher quando effettua il logout e viene invocato questo metodo
                getUI().get().access(() -> {
                    if(chatContainerDialog.isOpened())
                        chatContainerDialog.close();
                    reset();
                    removeAll();
                    if (account.getTypeAccount().equals("student")) {
                        DialogUtility dialogUtility = new DialogUtility();
                        //dialogUtility.partitaTerminataFromTeacher();
                        endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, false, new Gruppo(), "");
                    } else {
                        endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, true, new Gruppo(), "");
                    }
                });
            }else{
                System.out.println("MatyUI.terminaPartitaFromTeacher() - getUI is not present for Account: " + account.getNome());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //Implementazione metodi della Java interface 'ChatListener'
    @Override
    public void receiveBroadcast(Gruppo g, String message) { //g e' il gruppo da cui e' stato inviato il messaggio
        getUI().get().access(() -> {
            chatUI.receiveBroadcast(g, message);
        });
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

        if(chatContainerDialog.isOpened())
            chatContainerDialog.close();
        System.out.println("MatyUI.browserIsLeaving() e' stato invocato; Account:" + account.getNome());

        if(account.getTypeAccount().equals("teacher")){ //teacher ha effettuato il logout, allora termina per tutti;
            BroadcasterMaty.terminaPartitaFromTeacher();
        }else if(BroadcasterMaty.getListeners().size() > 1) { //se rimuovendo questo utente dal listener, sono presenti almeno 2 account
            BroadcasterMaty.unregister(account, this);
            endGamePublisher.doStuffAndPublishAnEvent("Maty", account, false, new Gruppo(), "");
        }else{  //nessun utente e' connesso, quindi termina la partita per tutti gli utenti connessi rimanenti
            BroadcasterMaty.terminaPartitaFromTeacher();
        }
    }

}