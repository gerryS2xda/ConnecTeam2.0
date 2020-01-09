
package com.example.demo.games.maty.frondend;


import com.example.demo.chat.BroadcasterChat;
import com.example.demo.chat.ChatListener;
import com.example.demo.chat.ChatUI;
import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.entity.Partita;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.gamesManagement.Maty;
import com.example.demo.games.maty.backend.MatyController;
import com.example.demo.games.maty.backend.broadcaster.BroadcasterMaty;
import com.example.demo.games.maty.backend.broadcaster.BroadcasterSuggerisciMaty;
import com.example.demo.games.maty.backend.db.ItemMaty;
import com.example.demo.games.maty.backend.listeners.BroadcastListenerMaty;
import com.example.demo.users.operation.NavBar;
import com.example.demo.users.event.EndGameEventBeanPublisher;
import com.example.demo.utility.*;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
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
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.erik.TimerBar;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Push
@Route("maty")
@HtmlImport("chat.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/chat.css")
@StyleSheet("frontend://stile/divbox.css")
@StyleSheet("frontend://stile/animation.css")
@StyleSheet("frontend://stile/matyStyle.css")
@JavaScript("frontend://js/script.js")
@PageTitle("ConnecTeam-Maty")
public class MatyUI extends VerticalLayout implements BroadcastListenerMaty, ChatListener, PageConfigurator, BeforeLeaveObserver {

    //static field
    public static Gruppo currentGroupSelect;

    //instance field
    private AccountRepository accountRepository;
    private Account account;
    private MatyController matyController;
    private Maty maty;
    private PartitaRepository partitaRepository;
    private Partita partita;
    private MatyController.PartitaThread partitaThread;
    private boolean isStarted = false;
    private boolean isTeacher = false;
    private Label labelQuesito;
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
    private VerticalLayout mainUIGame;
    private HorizontalLayout secondiContainer;
    private TimerBar timerBar;
    private VerticalLayout aiutoContainer;
    private Label titleGruppi;
    private ItemMaty item;


    public MatyUI(@Autowired EndGameEventBeanPublisher endGameEventBeanPublisher) {

        try {
            //Inizializzazione UI
            setId("MatyUI");
            setWidth("100%");
            setHeight("100%");
            setPadding(false);

            //Inizializzazione field
            maty = new Maty();
            endGamePublisher = endGameEventBeanPublisher;
            maxNumeroStutentiConnessi = com.example.demo.users.broadcaster.Broadcaster.getNumberOfMatyUser();
            teacherSession = com.example.demo.users.broadcaster.Broadcaster.getTeacherSession();
            gruppi = com.example.demo.users.broadcaster.Broadcaster.getGruppiListReceive();
            currentGroupSelect = new Gruppo();
            currentGroupSelect.setId("Gruppo 1"); //per default viene selezionato il 'Gruppo 1'

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
                return;
            }

            if(account.getTypeAccount().equals("teacher")) {
                isTeacher = true;
                //UI.getCurrent().setPollInterval(1000); //Per il teacher: Da usare solo le pagina viene caricata con UI.navigate(...)
            }else{
                DialogUtility dialogUtility = new DialogUtility();
                attendiDialog = dialogUtility.showDialog("Attendere...", "black");
                attendiDialog.open();
            }

            //Init StartGameMatyUI
            matyController = new MatyController(partitaRepository);
            startGameMatyUI = new StartGameMatyUI(matyController, account, isTeacher);

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

            //Chat container
            chatUI = new ChatUI(new Maty(), account, accountRepository, gruppi);
            chatContainerDialog = createDialogWithChatContent();

            if(isTeacher){ //mostra la appbar
                setSpacing(false);
                getStyle().set("margin", "0");
                appBarUI = new AppBarUI("Maty", false, true); //nome pagina corrente
                add(appBarUI);
                showButtonInAppBar();
            }else{
                //mostra la navBar orizzontale se e' stutente
                navBar = new NavBar(true);
                navBar.addClassName("navBarHorizontal");
                navBar.getElement().getStyle().set("height", "70px");
                navBar.getHomeContainerWithBtn().addClassName("navBarItemStyle");
                navBar.getLogOutContainer().getStyle().set("top", "8px");
                navBar.getLogOutContainer().addClassName("navBarItemStyle");
                add(navBar);
                addInfoBtnInNavBar();
                addChatBtnInNavBar();
            }

            if(!isTeacher) {
                Label nameGameTitle = new Label("Maty");
                nameGameTitle.addClassName("nameGameTitle");
                add(nameGameTitle);
            }else{
                titleGruppi = new Label("");
                titleGruppi.addClassName("titleGruppiTeacher");
                add(titleGruppi);
                setMenuItemClickEventForGruppiMenuItem("Gruppo 1"); //gruppo 1 e' di default
            }

            //Init main container
            mainUIGame = new VerticalLayout();
            mainUIGame.setPadding(false);
            if(!isTeacher){
                mainUIGame.addClassName("mainUIGameVL");
            }else{
                mainUIGame.addClassName("mainUIGameVLTeacher");
            }

            add(mainUIGame);

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
                    matyController.setItemMatyBeforeStartGame();
                    item = matyController.getItem();
                    BroadcasterMaty.addItemMaty(item);

                    matyController.startGame(partita);
                    partitaThread = matyController.getPartitaThread();
                    BroadcasterMaty.startGame(partitaThread);
                } else {
                    InfoEventUtility infoEventUtility = new InfoEventUtility();
                    infoEventUtility.infoEvent("C'è una partita in corso aspetta che finisca", "10");
                }
            });
            add(start);

            waitAllUserForStartGame();

        }
        catch (Exception e) {
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
        System.out.println("waitAllUserForStartGame(): Listener: " + BroadcasterMaty.getListeners().size() + " maxNumeroStutentiConnessi: " + maxNumeroStutentiConnessi);

        //Blocca esecuzione finche' tutti gli studenti (incluso il teacher) non sono connessi a questa pagina
        while(BroadcasterMaty.getListeners().size() <= maxNumeroStutentiConnessi);

        if(account.getTypeAccount().equals("teacher")) {
            if(!isStarted){
                start.click();
            }
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
            BroadcasterMaty.terminaPartitaForAll("Partita terminata dal teacher");
            com.example.demo.users.broadcaster.Broadcaster.setCountMatyUser(0); //reset counter giocatori di Maty
        });

        horizontalLayout.add(createMenuBarForGruppi(), infoBtn, chatBtn, terminateGame);
        appBarUI.add(horizontalLayout);
    }

    private void addChatBtnInNavBar(){
        HorizontalLayout chatContainer = new HorizontalLayout();
        chatContainer.getElement().setAttribute("id", "chatContainerNVMatyUI");
        chatContainer.addClassName("navBarItemStyle");

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
        infoContainer.addClassName("navBarItemStyle");

        Icon infoIcon = new Icon(VaadinIcon.INFO_CIRCLE_O);
        infoIcon.setSize("28px");
        infoIcon.setColor("#007d99");
        infoIcon.getStyle().set("margin-top", "3px");

        Image logoGuess = new Image("frontend/img/Guess.jpeg", "guess");
        DialogUtility dialogUtility = new DialogUtility();
        Dialog d = dialogUtility.descrizioneGiocoDialog(maty, logoGuess);
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

        startGameMatyUI.refreshContentForTeacher();

        String groupId = currentGroupSelect.getId();

        startGameMatyUI.hideAllContainerForTeacher();

        Div containerBox = Utils.getDivFromListByAttribute(startGameMatyUI.getContainersBoxTeacher(), "name", groupId);
        containerBox.getStyle().set("display", "block");

        VerticalLayout numeroInseritoVL = Utils.getVerticalLayoutFromListByAttribute(startGameMatyUI.getNumeroInseritoVLTeacherList(), "name", groupId);
        numeroInseritoVL.getStyle().set("display", "flex");

        startGameMatyUI.showSelectedCronologiaNumeriGridTeacher(groupId);

        chatUI.hideAllSpazioMessaggiTeacher();
        MessageList spaziomsgTeacher = Utils.getMessageListFromListByAttributeForChat(chatUI.getSpazioMessaggiTeacher(), "name", groupId);
        spaziomsgTeacher.getStyle().set("display", "block"); //mostra solo DivContainerPVTeacher in base al currentGroupSelect

    }

    //public methods
    public static void reset(){
        try {
            BroadcasterMaty.getIntegers().clear();
            BroadcasterMaty.clearPartiteThread();   //interrompi tutti i thread dedicati alle partite e poi fai clear della List
            BroadcasterSuggerisciMaty.getItems().clear();
            BroadcasterSuggerisciMaty.reset();
            BroadcasterMaty.getAccountList().clear();
            BroadcasterMaty.getItems().clear();
            BroadcasterMaty.getContClick().clear();
            BroadcasterMaty.getListeners().clear();
            BroadcasterChat.getListeners().clear();
            BroadcasterSuggerisciMaty.getListeners().clear();
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
                        //Quesito da mostrare
                        labelQuesito = new Label();
                        labelQuesito.addClassName("labelQuesito");
                        mainUIGame.add(labelQuesito);

                        //Tempo
                        secondiContainer = new HorizontalLayout();
                        secondiContainer.setWidth("100%");
                        Label lab = new Label("Tempo: ");
                        lab.getStyle().set("font-size","30px");
                        lab.getStyle().set("margin-left","15px");

                        timerBar = new TimerBar(300000); //5 minuti
                        timerBar.getElement().getStyle().set("width", "100%");
                        timerBar.getElement().getStyle().set("min-width", "50%");   //valore minimo precedente: 500px
                        timerBar.getElement().getStyle().set("margin-top", "14px");

                        secondiContainer.add(lab, timerBar);
                        mainUIGame.add(secondiContainer);

                        //Aiuto container
                        aiutoContainer = new VerticalLayout();
                        aiutoContainer.getStyle().set("display", "none"); //all'inizio non viene reso visibile
                        aiutoContainer.setPadding(false);
                        mainUIGame.add(aiutoContainer);

                        //StartGameMatyUI: HorizontalLayout container
                        //startGameMatyUI = new StartGameMatyUI(matyController, account, isTeacher);
                        startGameMatyUI.initItemMaty();
                        mainUIGame.add(startGameMatyUI);

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
                Label aiutolabel = new Label("Suggerimento: " + message);
                aiutolabel.addClassName("aiutiLabelStyle");
                aiutoContainer.getStyle().set("display", "flex");
                aiutoContainer.add(aiutolabel);
            }
        });
    }

    @Override
    public void countDown(int time) {
        getUI().get().access(() -> {
            if(time == 300){
                timerBar.start();
            }
        });
    }

    @Override
    public void partitaVincente(String parola, int punteggio) {
        getUI().get().access(() -> {
            //reset();
            if(chatContainerDialog.isOpened())
                chatContainerDialog.close();
            removeAll();
            if(account.getTypeAccount().equals("student")) {
                FireWorks fireWorks = new FireWorks();
                add(fireWorks);
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitaVincente(parola, punteggio, maty);
                endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, false, new Gruppo(), "");
            }
        });
    }

    @Override
    public void partitaVincenteForTeacher(Gruppo gruppo){
        getUI().get().access(() -> {
            if(chatContainerDialog.isOpened())
                chatContainerDialog.close();
            endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, false, gruppo, "vincente");
        });
    }

    @Override
    public void partitaNonVincente() {
        getUI().get().access(() -> {
            //reset();
            if(chatContainerDialog.isOpened())
                chatContainerDialog.close();
            removeAll();
            if(account.getTypeAccount().equals("student")) {
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitanonVincente(maty);
                endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, false, new Gruppo(), "");
            }
        });
    }

    @Override
    public void partitaNonVincenteForTeacher(Gruppo gruppo){
        getUI().get().access(() -> {
            if(chatContainerDialog.isOpened())
                chatContainerDialog.close();
            endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, false, gruppo, "non-vincente");
        });
    }

    @Override
    public void numeroDaSotrarre(String numero,String numOriginzle) {
        getUI().get().access(() -> {
            labelQuesito.setText("Raggiungi "+numOriginzle+" sottraendo " +BroadcasterMaty.getListeners().size()+" sottraendi a "+numero +"!");
        });
    }

    @Override
    public void numeroDaSommare(String numOriginzle) {
        getUI().get().access(() -> {
            labelQuesito.setText("Raggiungi "+numOriginzle+" sommando " +BroadcasterMaty.getListeners().size()+" addendi!");
        });
    }

    @Override
    public void terminaPartitaForAll(String msgDialog) {  //usato anche per indicare se non ci sono piu' utenti connessi
        try {
            if(getUI().isPresent()) {   //inserito per evitare exception (No value Present) dovuta al teacher quando effettua il logout e viene invocato questo metodo
                getUI().get().access(() -> {
                    if(chatContainerDialog.isOpened())
                        chatContainerDialog.close();
                    reset();
                    removeAll();
                    if (account.getTypeAccount().equals("student")) {
                        DialogUtility dialogUtility = new DialogUtility();
                        dialogUtility.partitaTerminataForAll(msgDialog);
                        endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, false, new Gruppo(), "");
                    } else {
                        endGamePublisher.doStuffAndPublishAnEvent(maty.getNomeGioco(), account, true, new Gruppo(), "");
                    }
                });
            }else{
                System.out.println("MatyUI.terminaPartitaForAll() - getUI is not present for Account: " + account.getNome());
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

        System.out.println("MatyUI.browserIsLeaving() e' stato invocato; Account:" + account.getNome());
        if(chatContainerDialog.isOpened())
            chatContainerDialog.close();

        BroadcasterMaty.unregister(account, this);

        if(account.getTypeAccount().equals("teacher")){ //teacher ha effettuato il logout, allora termina per tutti;
            BroadcasterMaty.terminaPartitaForAll("Partita terminata!! Teacher si e' disconnesso");
            return;
        }

        if(BroadcasterMaty.getListeners().size() > 1) { //se rimuovendo questo utente dal listener, sono presenti almeno 2 account
            endGamePublisher.doStuffAndPublishAnEvent("Maty", account, false, new Gruppo(), "");
        }else{  //nessun utente e' connesso, quindi termina la partita per tutti gli utenti connessi rimanenti
            BroadcasterMaty.terminaPartitaForAll("Partita terminata!! Nessun utente e' connesso al gioco");
        }
    }

    //Implements methods of BeforeLeaveObserver
    @Override
    public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) { //Necessario poiche' viene invocato UI.navigate() dal server e NON dal client
        System.out.println("MatyUI.beforeLeave() e' stato invocato; Account:" + account.getNome());

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

        BroadcasterMaty.unregister(account, this);

        if(!isTeacher && BroadcasterMaty.getListeners().size() > 1) { //se rimuovendo questo utente dal listener, sono presenti almeno 2 account
            endGamePublisher.doStuffAndPublishAnEvent("Maty", account, false, new Gruppo(), "");
        }else{  //nessun utente e' connesso, quindi termina la partita per tutti gli utenti connessi rimanenti
            BroadcasterMaty.terminaPartitaForAll("Partita terminata!! Nessun utente e' connesso al gioco");
        }
    }

}