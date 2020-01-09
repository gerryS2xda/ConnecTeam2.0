package com.example.demo.guess.gamesMenagemet.frondend;

import com.example.demo.chat.ChatUI;
import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.entity.Partita;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.gamesManagement.Guess;
import com.example.demo.guess.gamesMenagemet.backend.GuessController;
import com.example.demo.guess.gamesMenagemet.backend.broadcaster.BroadcasterGuess;
import com.example.demo.chat.BroadcasterChat;
import com.example.demo.guess.gamesMenagemet.backend.broadcaster.BroadcasterSuggerisci;
import com.example.demo.guess.gamesMenagemet.backend.db.Item;
import com.example.demo.guess.gamesMenagemet.backend.listeners.BroadcastListener;
import com.example.demo.chat.ChatListener;
import com.example.demo.users.operation.NavBar;
import com.example.demo.users.event.EndGameEventBeanPublisher;
import com.example.demo.utility.*;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.erik.TimerBar;
import java.sql.Timestamp;
import java.util.*;

@Push
@Route("guess")
@HtmlImport("chat.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/guessStyle.css")
@StyleSheet("frontend://stile/chat.css")
@PageTitle("ConnecTeam-Guess")
public class GuessUI extends HorizontalLayout implements BroadcastListener, ChatListener, PageConfigurator, BeforeLeaveObserver {

    //static field
    public static Gruppo currentGroupSelect;
    private static List<Gruppo> gruppi = new ArrayList<Gruppo>();

    //instance field
    private Account account;
    private AccountRepository accountRepository;
    private PartitaRepository partitaRepository;
    private Partita partita;
    private GuessController.PartitaThread partitaThread;
    private Item item;
    private Guess guess;
    private GuessController guessController;
    private HorizontalLayout secondiContainer;
    private VerticalLayout indiziContainer;
    private HorizontalLayout startGameUIContainer = new HorizontalLayout();
    private boolean isStarted = false;
    private boolean isTeacher = false;
    private EndGameEventBeanPublisher endGamePublisher;
    //Numero di utenti connessi al momento in cui il teacher da' il via alla partita
    private int maxNumeroStutentiConnessi = 0;
    private AppBarUI appBarUI;
    private WrappedSession teacherSession;
    private Button start; //pulsante che sara'
    private Dialog attendiDialog;
    private ArrayList<Div> containersPVTeacher; //container parole votate per teacher
    private ArrayList<Div> containersParoleVotate; //container parole votate per student
    private StartGameUI startGameUI;
    private NavBar navBar;
    private Dialog chatContainerDialog;
    private ChatUI chatUI;
    private TimerBar timerBar;
    private Div containerParoleVotateMain = new Div(); //presente in StartGameUI
    private Label titleGruppi;

    //Constructor
    public GuessUI(@Autowired EndGameEventBeanPublisher endGameEventBeanPublisher) {

        try {
            //Inizializzazione
            setId("GuessUI");
            setWidth("100%");
            setHeight("100%");
            guess = new Guess();
            endGamePublisher = endGameEventBeanPublisher;
            maxNumeroStutentiConnessi = com.example.demo.users.broadcaster.Broadcaster.getNumberOfGuessUser();
            teacherSession = com.example.demo.users.broadcaster.Broadcaster.getTeacherSession();
            currentGroupSelect = new Gruppo();
            currentGroupSelect.setId("Gruppo 1"); //per default viene selezionato il 'Gruppo 1'
            containersPVTeacher = new ArrayList<Div>();
            containersParoleVotate = new ArrayList<Div>();

            UI.getCurrent().getElement().getStyle().set("overflow", "hidden"); //accedi al <body> element

            if(VaadinService.getCurrentRequest() != null) {
                //Ottieni valori dalla sessione corrente e verifica se sono presenti in sessione
                account = (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
                accountRepository = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
                partitaRepository = (PartitaRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("partitaRepository");
            }else{ //getCurrentRequest() is null (poiche' e' il server che 'impone' accesso a questa pagina - no memorizzazione stato partita)
                account = (Account) teacherSession.getAttribute("user");
                accountRepository = (AccountRepository) teacherSession.getAttribute("rep");
                partitaRepository = (PartitaRepository) teacherSession.getAttribute("partitaRepository");
            }

            if(account == null || accountRepository == null || partitaRepository == null){
                showErrorPage();
                return; //non fare nulla
            }

            if(account.getTypeAccount().equals("teacher")) {
                isTeacher = true;
                //UI.getCurrent().setPollInterval(1000); Per il teacher: da usare solo se la pagina viene caricata con UI.navigate(...)
            }

            if(gruppi.size() == 0){ //la lista dei gruppi e' stata settata una sola volta?
                gruppi = com.example.demo.users.broadcaster.Broadcaster.getGruppiListReceive();
            }

            //Inizializzazione per StartGameUI
            guessController = new GuessController(partitaRepository);
            startGameUI = new StartGameUI(guessController, isTeacher, account);


            //Per ogni partita gia' iniziata, setta isStarted a true (una sola partita alla volta)
            for (int i = 0; i < BroadcasterGuess.getPartiteThread().size(); i++) {
                if (BroadcasterGuess.getPartiteThread().get(i) != null) {
                    isStarted = true;
                }
            }

            if (isStarted != true) {
                BroadcasterGuess.register(account, this);
                BroadcasterChat.register(account, this);
            } else {
                System.out.println("GUESSUI:TEST1 Account: " + account.getNome());
                if(account.getTypeAccount().equals("student")){
                    DialogUtility dialogUtility = new DialogUtility();
                    dialogUtility.showDialogPartitaInCorso(guess.getNomeGioco(), "C'è una partita in corso... Attendere la fine della partita").open();
                }
                //NOTA: per il teacher, quando esce dalla pagina, viene invocato TeacherMainUITab.beforeLeave() e la partita termina per tutti
                return;
            }

            if(isTeacher){ //mostra la appbar
                getStyle().set("width", "100%");
                appBarUI = new AppBarUI("Guess", false, true); //nome pagina corrente
                add(appBarUI);
                showButtonInAppBar();
            }else{
                //mostra la navBar orizzontale se e' stutente
                navBar = new NavBar(true);
                navBar.addClassName("navBarHorizontal");
                add(navBar);
                addInfoBtnInNavBar();
                addChatBtnInNavBar();

                //mostra un dialog 'Attendere' per indicare il caricamento della pagina
                DialogUtility dialogUtility = new DialogUtility();
                attendiDialog = dialogUtility.showDialog("Attendere...", "black");
                attendiDialog.open();
            }

            //Chat container
            chatUI = new ChatUI(new Guess(), account, accountRepository, gruppi);
            chatContainerDialog = createDialogWithChatContent();

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

            if(!isTeacher) {
                Label nameGameTitle = new Label("Guess");
                nameGameTitle.addClassName("nameGameTitle");
                add(nameGameTitle);
            }else{
                titleGruppi = new Label("");
                titleGruppi.addClassName("titleGruppi");
                add(titleGruppi);
                setMenuItemClickEventForGruppiMenuItem("Gruppo 1"); //gruppo 1 e' di default
            }

            start = new Button();
            start.getStyle().set("display", "none");
            start.addClickListener(buttonClickEvent -> {
                System.out.println("GuessUI: Partita iniziata!");
                for (int i = 0; i < BroadcasterGuess.getPartiteThread().size(); i++) {
                    if (BroadcasterGuess.getPartiteThread().get(i) != null) {
                        isStarted = true;
                    }
                }
                if (isStarted != true) {
                    partita = new Partita(new Timestamp(new Date().getTime()), "Guess");
                    guessController.startGame(partita);
                    partitaThread = guessController.getPartitaThread();
                    item = guessController.getItem();
                    BroadcasterGuess.startGame(UI.getCurrent(), partitaThread, item);
                } else {
                    System.out.println("GUESSUI:TEST2");
                    if(account.getTypeAccount().equals("student")){
                        DialogUtility dialogUtility = new DialogUtility();
                        dialogUtility.showDialogPartitaInCorso(guess.getNomeGioco(), "C'è una partita in corso... Attendere la fine della partita").open();
                    }
                    //NOTA: per il teacher, quando esce dalla pagina, viene invocato TeacherMainUITab.beforeLeave() e partita termina per tutti
                    return;
                }
            });
            add(start);

            waitAllUserForStartGame();

        }catch (Exception e) {
            showErrorPage();
            e.printStackTrace();
        }
    }

    private void showErrorPage(){
        removeAll();
        ErrorPage errorPage = new ErrorPage();
        add(errorPage);
    }

    //Inizia una partita solo quando il teacher e alcuni studenti sono connessi a questa pagina
    private void waitAllUserForStartGame(){
        //Blocca esecuzione finche' tutti gli studenti (incluso il teacher) non sono connessi a questa pagina
        while(BroadcasterGuess.getListeners().size() <= maxNumeroStutentiConnessi);

        if(account.getTypeAccount().equals("teacher")) {
            if(!isStarted){
                start.click();
            }
        }
    }

    private void hideAllContainerPVTeacher(){
        for(Div i : containersPVTeacher){
            i.getStyle().set("display", "none");
        }
    }

    private void showButtonInAppBar(){
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.getStyle().set("position", "absolute");
        horizontalLayout.getStyle().set("left", "70%");
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
            Image logoGuess = new Image("frontend/img/Guess.jpeg", "guess");
            dialogUtility.descrizioneGiocoDialog(new Guess(), logoGuess).open();
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
            BroadcasterGuess.terminaPartitaForAll("Partita terminata dal teacher");
            com.example.demo.users.broadcaster.Broadcaster.setCountGuessUser(0); //reset counter giocatori di Guess
        });

        horizontalLayout.add(createMenuBarForGruppi(), infoBtn, chatBtn, terminateGame);
        appBarUI.add(horizontalLayout);
    }

    private void addChatBtnInNavBar(){
        HorizontalLayout chatContainer = new HorizontalLayout();
        chatContainer.getElement().setAttribute("id", "chatContainerNVGuessUI");

        Icon chatIcon = new Icon(VaadinIcon.CHAT);
        chatIcon.setSize("30px");
        chatIcon.setColor("#007d99");
        chatIcon.getStyle().set("margin-top", "3px");

        Button chatBtn = new Button("Chat");
        chatBtn.addClassName("buttonChatStyleNavBar");
        chatBtn.addClickListener(buttonClickEvent -> {
            chatContainerDialog.open();
        });

        chatContainer.add(chatIcon, chatBtn);
        navBar.getChatContainer().add(chatContainer);
    }

    private void addInfoBtnInNavBar(){
        HorizontalLayout infoContainer = new HorizontalLayout();
        infoContainer.getElement().setAttribute("id", "infoContainerNVGuessUI");

        Icon infoIcon = new Icon(VaadinIcon.INFO_CIRCLE_O);
        infoIcon.setSize("28px");
        infoIcon.setColor("#007d99");
        infoIcon.getStyle().set("margin-top", "3px");

        Image logoGuess = new Image("frontend/img/Guess.jpeg", "guess");
        DialogUtility dialogUtility = new DialogUtility();
        Dialog d = dialogUtility.descrizioneGiocoDialog(guess, logoGuess);
        Button infoBtn = new Button("Info");
        infoBtn.addClassName("buttonInfoStyleNavBar");
        infoBtn.addClickListener(buttonClickEvent -> {
            d.open();
        });

        infoContainer.add(infoIcon, infoBtn);
        navBar.getInfoGameContainer().add(infoContainer);
    }

    private Dialog createDialogWithChatContent(){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setWidth("100%");
        d.setHeight("100%");

        d.add(chatUI);

        return d;
    }

    private MenuBar createMenuBarForGruppi(){
        MenuBar menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
        menuBar.getStyle().set("background-color", "#0000");
        menuBar.getStyle().set("margin", "0");

        Icon gruppiIcon = new Icon(VaadinIcon.GROUP);
        gruppiIcon.setSize(AppBarUI.ICON_BTN_SIZE);

        MenuItem gruppiItem = menuBar.addItem("Gruppi");
        gruppiItem.addComponentAsFirst(gruppiIcon);
        SubMenu gruppiSubMenu = gruppiItem.getSubMenu();

        for(Gruppo g : gruppi){
            gruppiSubMenu.addItem(g.getId(), menuItemClickEvent -> {
                String value = g.getId();
                setMenuItemClickEventForGruppiMenuItem(value);
            });
        }

        return menuBar;
    }

    private void setMenuItemClickEventForGruppiMenuItem(String value){
        titleGruppi.setText(value);
        currentGroupSelect = Utils.findGruppoByName(gruppi, value);

        hideAllContainerPVTeacher();
        Div containerPVteacher = Utils.getDivFromListByAttribute(containersPVTeacher, "name", currentGroupSelect.getId());
        containerPVteacher.getStyle().set("display", "block"); //mostra solo DivContainerPVTeacher in base al currentGroupSelect

        startGameUI.showParolaSuggeritaAndBtnTeacher(currentGroupSelect.getId());

        chatUI.hideAllSpazioMessaggiTeacher();
        MessageList spaziomsgTeacher = Utils.getMessageListFromListByAttributeForChat(chatUI.getSpazioMessaggiTeacher(), "name", currentGroupSelect.getId());
        spaziomsgTeacher.getStyle().set("display", "block"); //mostra solo DivContainerPVTeacher in base al currentGroupSelect
    }

    //public methods
    public static List<Gruppo> getListGruppi() {
        return gruppi;
    }

    public static void reset(){  //eseguito solo quando la partita termina da parte del teacher o quando non ci sono piu' utenti connessi
        try {
            BroadcasterGuess.clearPartiteThread();   //interrompi tutti i thread sulle partite e poi fai clear della List
            BroadcasterGuess.getVotes().clear();
            BroadcasterGuess.getAccountList().clear();
            BroadcasterGuess.getItems().clear();
            BroadcasterGuess.getListeners().clear();
            BroadcasterChat.getListeners().clear();
            BroadcasterSuggerisci.getListeners().clear();
            BroadcasterGuess.getParoleVotateHM().clear();
            gruppi.clear();
        }catch(Exception e){
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

    //Implementazione metodi della Java interface 'BroadcasterListener'
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

                        secondiContainer = new HorizontalLayout();
                        secondiContainer.setWidth("100%");
                        if(isTeacher){
                            secondiContainer.addClassName("secondiContainerTeacher");
                        }else{
                            secondiContainer.addClassName("secondiContainer");
                        }
                        Label lab = new Label("Prossimo indizio tra: ");
                        lab.getStyle().set("font-size","30px");
                        lab.getStyle().set("margin-left","15px");
                        timerBar = new TimerBar(30000);
                        timerBar.getElement().getStyle().set("width", "200px");
                        timerBar.getElement().getStyle().set("margin-top", "14px");
                        secondiContainer.add(lab, timerBar);
                        add(secondiContainer);

                        indiziContainer = new VerticalLayout();
                        indiziContainer.setPadding(false);
                        indiziContainer.setWidth("100%");
                        if(isTeacher){
                            indiziContainer.addClassName("indiziContainerTeacher");
                        }else{
                            indiziContainer.addClassName("indiziContainer");
                        }
                        Label lab1 = new Label("Indizi");
                        lab1.getStyle().set("font-size", "30px");
                        lab1.getStyle().set("margin-left", "15px");
                        indiziContainer.add(lab1);

                        startGameUIContainer.add(indiziContainer);

                        //StartGameUI startGameUI = new StartGameUI(guessController, isTeacher, account);
                        startGameUIContainer.add(startGameUI);
                        add(startGameUIContainer);

                        containerParoleVotateMain = startGameUI.getContainerParoleVotateMain();
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
    public void receiveIndizio(int indexIndizio, String message) {
        getUI().get().access(() -> {
            Div d = new Div();
            d.addClassName("divContainerIndizio");
            switch (indexIndizio){
                case 0: d.getStyle().set("background-color", "#0C8BE8"); break;
                case 1: d.getStyle().set("background-color", "#0CE859"); break;  //oppure #23BAFF
                case 2: d.getStyle().set("background-color", "#4EEB00"); break;
                case 3: d.getStyle().set("background-color", "#FF7E1A"); break;
            }

            Paragraph paragraph = new Paragraph(message);
            paragraph.addClassName("pIndizio");
            d.add(paragraph);

            indiziContainer.add(d);
        });
    }

    @Override
    public void countDown(int time) {
        getUI().get().access(() -> {
            if(time == 30){
                timerBar.start();
            }else if(time == 0){
                timerBar.reset();
                timerBar.start();
            }

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

            BroadcasterGuess.getVotes().forEach((s, integer) -> {
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
                    messageList.addClassName("divMessageListParolaVotata");
                    messageList.add(label);
                    containerPV.add(messageList);
                    containerPV.getStyle().set("display", "block");
                    containerParoleVotateMain.add(containerPV);
                    //add(containerPV);
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

            BroadcasterGuess.getVotes().forEach((s, integer) -> {
                String a = "";
                if(integer == 1){
                    a = " voto";
                }else
                    a = " voti";

                Label label = new Label(s + " ha " + integer + a);
                label.getElement().setAttribute("id", "parolaVotata");
                MessageList msgList = new MessageList("message-list");
                msgList.addClassName("divMessageListParolaVotata");
                msgList.getElement().setAttribute("id", "PV" + gruppo.getId());
                msgList.add(label);
                containerPVTeacher.add(msgList);
                containerParoleVotateMain.add(containerPVTeacher);
                //add(containerPVTeacher);
            });
        });
    }

    @Override
    public void partitaVincente(String parola,int punteggio) {
        getUI().get().access(() -> {
            //reset();
            if(chatContainerDialog.isOpened())
                chatContainerDialog.close();
            removeAll();
            if(account.getTypeAccount().equals("student")) {
                FireWorks fireWorks = new FireWorks();
                add(fireWorks);
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitaVincente(parola, punteggio, guess);
                endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false, new Gruppo(), "");
            }
        });
    }

    @Override
    public void partitaVincenteForTeacher(Gruppo gruppo){
        getUI().get().access(() -> {
            if(chatContainerDialog.isOpened())
                chatContainerDialog.close();
            endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false, gruppo, "vincente");
        });
    }

    @Override
    public void partitaNonVincente(){
        getUI().get().access(() -> {
            //reset();
            if(chatContainerDialog.isOpened())
                chatContainerDialog.close();
            removeAll();
            if(account.getTypeAccount().equals("student")) {
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitanonVincente(guess);
                endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false, new Gruppo(), "");
            }
        });
    }

    @Override
    public void partitaNonVincenteForTeacher(Gruppo gruppo){
        getUI().get().access(() -> {
            if(chatContainerDialog.isOpened())
                chatContainerDialog.close();
            endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false, gruppo, "non-vincente");
        });
    }

    @Override
    public void terminaPartitaForAll(String msgDialog) {  //usato anche per indicare se non ci sono piu' utenti connessi
        try {
            if(getUI().isPresent()) {   //inserito per evitare exception (No value Present) dovuta al teacher quando effettua il logout e viene invocato questo metodo
                getUI().get().access(() -> {
                    reset();
                    if(chatContainerDialog.isOpened())
                        chatContainerDialog.close();
                    removeAll();
                    if (account.getTypeAccount().equals("student")) {
                        DialogUtility dialogUtility = new DialogUtility();
                        dialogUtility.partitaTerminataForAll(msgDialog);
                        endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false, new Gruppo(), "");
                    } else{
                        endGamePublisher.doStuffAndPublishAnEvent("Guess", account, true, new Gruppo(), "");
                    }
                });
            }else{
                System.out.println("GuessUI.terminaPartitaForAll() - getUI is not present for Account: " + account.getNome());
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
        for(Account i : BroadcasterGuess.getListeners().keySet()){
            if(i.equals(account)){
                flag = true;
                break;
            }
        }
        if(!flag){  //se il listener non contiene questo 'account' -> non fare nulla
            return;
        }

        System.out.println("GuessUI.browserIsLeaving() e' stato invocato; Account:" + account.getNome());
        if(chatContainerDialog.isOpened())
            chatContainerDialog.close();

        BroadcasterGuess.unregister(account, this);

        if(account.getTypeAccount().equals("teacher")){ //teacher ha effettuato il logout, allora termina per tutti;
            BroadcasterGuess.terminaPartitaForAll("Partita terminata!! Teacher si e' disconnesso");
            return;
        }

        if(BroadcasterGuess.getListeners().size() > 1) { //se rimuovendo questo utente dal listener, sono presenti almeno 2 account
            endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false, new Gruppo(), "");
        }else{  //nessun utente e' connesso, quindi termina la partita per tutti gli utenti connessi rimanenti
            BroadcasterGuess.terminaPartitaForAll("Partita terminata!! Nessun utente e' connesso al gioco");
        }
    }

    //Implements methods of BeforeLeaveObserver
    @Override
    public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) { //Necessario poiche' viene invocato UI.navigate() dal server e NON dal client
        System.out.println("GuessUI.beforeLeave() e' stato invocato; Account:" + account.getNome());

        //Pre-condition
        boolean flag = false;
        for(Account i : BroadcasterGuess.getListeners().keySet()){
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

        BroadcasterGuess.unregister(account, this);
        if(!isTeacher && BroadcasterGuess.getListeners().size() > 1) { //se rimuovendo questo utente dal listener, sono presenti almeno 2 account
            endGamePublisher.doStuffAndPublishAnEvent("Guess", account, false, new Gruppo(), "");
        }else{  //nessun utente e' connesso, quindi termina la partita per tutti gli utenti connessi rimanenti
            BroadcasterGuess.terminaPartitaForAll("Partita terminata!! Nessun utente e' connesso al gioco");
        }
    }
}
