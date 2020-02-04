package com.example.demo.users.controller;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.games.guess.backend.broadcaster.BroadcasterGuess;
import com.example.demo.games.guess.frondend.GuessUI;
import com.example.demo.mainView.MainView;
import com.example.demo.games.maty.backend.broadcaster.BroadcasterMaty;
import com.example.demo.games.maty.frondend.MatyUI;
import com.example.demo.users.operation.SettingsUser;
import com.example.demo.users.broadcaster.Broadcaster;
import com.example.demo.users.broadcaster.BroadcastListenerTeacher;
import com.example.demo.users.event.EndGameEventBeanPublisher;
import com.example.demo.users.event.StartGameEventBeanPublisher;
import com.example.demo.utility.DialogUtility;
import com.example.demo.utility.InfoEventUtility;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.NoSuchElementException;

@Push
@Route("TeacherHomeView")
@HtmlImport("style.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/navBarVertStyle.css")
@JavaScript("frontend://js/script.js")
@PageTitle("ConnecTeam")
public class TeacherMainUITab extends HorizontalLayout implements BroadcastListenerTeacher, BeforeLeaveObserver {

    //costanti
    private static final String icon_Size = "24px"; //valore precedente: 30px
    private static final String icon_color = "#007d99";
    public static final String NAVBAR_WIDTH = "252px";
    public static final String NAVBAR_HEIGHT = "100%";

    //instance field
    private Account account;
    private AccountRepository accountRepository;
    private ControllerMainUI mainView;
    private SettingsUser settingsView;
    private GestioneStudentUI gestStudentiView;
    private GuessUI guessView;
    private MatyUI matyView;
    private StartGameEventBeanPublisher startGameEventBeanPublisher;
    //instance field per elementi della navbar (usato per 'highlight' style -- indicare quale view viene mostrata)
    private Div home;
    private Div gestStud;
    private Div settings;
    private Div guess;
    private Div maty;
    private Div newGame;
    private EndGameEventBeanPublisher endGamePublisher;
    private boolean isShowErrorDialog = false;


    public TeacherMainUITab(@Autowired StartGameEventBeanPublisher startGameEventPublisher, @Autowired EndGameEventBeanPublisher endGameEventBeanPublisher){

        try{
            accountRepository = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
            account = (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
            if(accountRepository == null || account == null){
                showErrorPage();
                return;
            }
            if(!account.getTypeAccount().equals("teacher")){
                throw new IllegalArgumentException("Questo account non puo' accedere a questa pagina");
            }
            setId("TeacherMainUITabTest");

            String actualWebBrowser = VaadinSession.getCurrent().getBrowser().getBrowserApplication();
            //verifica se l'account che sta tentando di accedere e' gia' loggato su un altro browser
            if (checkIfAccountIsAlreadyLogged(actualWebBrowser)) {  //utilizza confronto tra browser attuale e quello memorizzato al primo accesso
                return; //necessario, altrimenti viene caricata la pagina anche se mostra il Dialog
            }


            //Registra un teacher listener, add teacherSession to List, add account in HashMap<Account, WebBrowser>
            Broadcaster.registerTeacher(account, this);
            Broadcaster.setTeacherSession(VaadinService.getCurrentRequest().getWrappedSession());
            Broadcaster.addNewAccountWithWebBrowser(account, VaadinSession.getCurrent().getBrowser());

            startGameEventBeanPublisher = startGameEventPublisher;
            endGamePublisher = endGameEventBeanPublisher; //for GuessUI istance

            UI.getCurrent().getElement().getStyle().set("overflow", "hidden"); //access al <body> element
            getStyle().set("height", "100%"); //per nav bar verticale
            getStyle().set("width", "100%");

            add(navBarVert());

            mainView = new ControllerMainUI();
            add(mainView);
            home.addClassName("highlight");

            UI.getCurrent().setPollInterval(1000); //Usato sia per GestioneStudentUI, GuessUI e MatyUI poiche' non vengono caricate con UI.navigate()
        }catch(Exception e){
            showErrorPage();
            e.printStackTrace();
        }
    }

    private void showErrorPage(){
        removeAll();
        ErrorPage errorPage = new ErrorPage();
        add(errorPage);
    }

    //private methods
    private boolean checkIfAccountIsAlreadyLogged(String actualWebBrowser){
        isShowErrorDialog = false;

        for (Account i : Broadcaster.getAccountWithWebBrowserHashMap().keySet()) {
            if (i.equals(account)) {   //se e' presente un account nella hashmap
                String str = Broadcaster.getAccountWithWebBrowserHashMap().get(i).getBrowserApplication();
                if(!actualWebBrowser.equals(str)) { //se il browser associato ad 'account' e' diverso da quello nella hashmap ad 'i'
                    //'account' proviene da un altro browser
                    DialogUtility dialogUtility = new DialogUtility();
                    dialogUtility.showErrorDialog("Errore", "L'utente che sta tentando di accedere al sito e' gia' loggato su un altro web browser!", "red");
                    isShowErrorDialog = true;
                    break;
                } //else: se sono uguali significa che si sta eseguendo un refresh della pagina
            }
        }
        return isShowErrorDialog;
    }

    //Implementazione navBar verticale da usare come 'Tab' per navigare tra le varie View
    public VerticalLayout navBarVert(){
        VerticalLayout vert = new VerticalLayout();
        vert.getStyle().set("width", NAVBAR_WIDTH);
        vert.getStyle().set("height", NAVBAR_HEIGHT);
        vert.getStyle().set("margin", "0px");
        vert.getStyle().set("padding", "0px");
        vert.addClassName("navi-drawer__content");

        Div navScrollArea = new Div();
        navScrollArea.addClassName("navi-drawer__scroll-area");
        navScrollArea.getStyle().set("width", NAVBAR_WIDTH);

        Div items = new Div(); //container di tutti gli item della navbar
        items.getStyle().set("margin-bottom", "0.5rem");
        items.getStyle().set("margin-top", "0.5 rem");

        Icon homeIcon = new Icon(VaadinIcon.HOME);
        homeIcon.setSize(icon_Size);
        homeIcon.setColor(icon_color);
        home = addDivContainerItem("Home", homeIcon);
        home.getElement().setAttribute("id", "homeNV");
        home.addClickListener(event -> {
            home.addClassName("highlight");
            mainView.getStyle().set("display", "flex"); //rendi nuovamente visibile
            if(settingsView != null){
                settingsView.getStyle().set("display", "none"); //non mostra elemento e neanche lo spazio richiesto
            }
            if(gestStudentiView != null){
                gestStudentiView.getStyle().set("display", "none");
            }
            if(guessView != null){
                guessView.getStyle().set("display", "none");
            }
            if(matyView != null){
                matyView.getStyle().set("display", "none");
            }
            gestStud.removeClassName("highlight");
            settings.removeClassName("highlight");
            guess.removeClassName("highlight");
            maty.removeClassName("highlight");
        });

        Icon settingIcon = new Icon(VaadinIcon.COGS);
        settingIcon.setSize(icon_Size);
        settingIcon.setColor(icon_color);
        settings = addDivContainerItem("Settings", settingIcon);
        settings.getElement().setAttribute("id", "settingsNV");
        settings.addClickListener(event -> {
            settings.addClassName("highlight");
            mainView.getStyle().set("display", "none"); //rendi nuovamente visibile
            if(gestStudentiView != null){
                gestStudentiView.getStyle().set("display", "none");
            }
            if(guessView != null){
                guessView.getStyle().set("display", "none");
            }
            if(matyView != null){
                matyView.getStyle().set("display", "none");
            }
            if(settingsView == null){
                settingsView = new SettingsUser();
                add(settingsView);
            }else{
                settingsView.getStyle().set("display", "flex");
            }
            home.removeClassName("highlight");
            gestStud.removeClassName("highlight");
            guess.removeClassName("highlight");
            maty.removeClassName("highlight");
        });

        Icon gestStudIcon = new Icon(VaadinIcon.EDIT);
        gestStudIcon.setSize(icon_Size);
        gestStudIcon.setColor(icon_color);
        gestStud = addDivContainerItem("Gestione Studenti", gestStudIcon);
        gestStud.getElement().setAttribute("id", "gestStudNV");
        gestStud.addClickListener(event -> {
            gestStud.addClassName("highlight");
            mainView.getStyle().set("display", "none"); //rendi nuovamente visibile
            if(settingsView != null){
                settingsView.getStyle().set("display", "none");
            }
            if(guessView != null){
                guessView.getStyle().set("display", "none");
            }
            if(matyView != null){
                matyView.getStyle().set("display", "none");
            }
            if(gestStudentiView == null){
                gestStudentiView = new GestioneStudentUI(startGameEventBeanPublisher, accountRepository, account, this);
                add(gestStudentiView);
            }else{
                gestStudentiView.getStyle().set("display", "flex");
            }
            home.removeClassName("highlight");
            settings.removeClassName("highlight");
            guess.removeClassName("highlight");
            maty.removeClassName("highlight");
        });

        Icon gamesIcon = new Icon(VaadinIcon.GAMEPAD);
        gamesIcon.setSize(icon_Size);
        gamesIcon.setColor(icon_color);
        gamesIcon.getStyle().set("margin-left", "0px"); //valore precedente: 32px (no accordion)
        Div gamesStud = addGamesListWithAccordion(gamesIcon);
        gamesStud.getElement().setAttribute("id", "gamesStudNV");

        items.add(home, settings, gestStud, gamesStud);
        navScrollArea.add(items);
        vert.add(addLogoContainer(), navScrollArea);
        return vert;
    }

    private Div addDivContainerItem(String linkText, Icon ic){
        Div d = new Div();
        d.addClassName("navi-item");
        d.getStyle().set("cursor", "pointer");
        RouterLink rl = new RouterLink();
        rl.addClassName("navi-item__link_logout");
        Span sp = new Span(linkText);
        if(ic != null)
            rl.add(ic, sp);
        else
            rl.add(sp);
        d.add(rl);
        return d;
    }

    private Div addGamesListWithAccordion(Icon ic){
        Div d = new Div();

        Accordion accordion = new Accordion();
        VerticalLayout panel1 = new VerticalLayout(); //contenuto del panel 1 dell'accordion
        panel1.setSpacing(false);
        panel1.setPadding(false);
        guess = addDivInAccordionPanelContent("Guess", null, "64px");
        guess.getElement().setAttribute("id", "guessNV");
        guess.addClickListener(event -> {
            if(Broadcaster.isGuessStart()) {
                guess.addClassName("highlight");
                mainView.getStyle().set("display", "none"); //rendi nuovamente visibile
                if (settingsView != null) {
                    settingsView.getStyle().set("display", "none");
                }
                if (gestStudentiView != null) {
                    gestStudentiView.getStyle().set("display", "none");
                }
                if(matyView != null){
                    matyView.getStyle().set("display", "none");
                }
                if (guessView == null) {
                    guessView = new GuessUI(endGamePublisher);
                    add(guessView);
                } else {
                    guessView.getStyle().set("display", "flex");
                }
                home.removeClassName("highlight");
                gestStud.removeClassName("highlight");
                settings.removeClassName("highlight");
                maty.removeClassName("highlight");
            }else{
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEventForTeacher("La partita non e' iniziata! Premi 'Avvia' in Gestione Studenti", "black", "420px");
            }
        });
        maty = addDivInAccordionPanelContent("Maty", null, "64px");
        maty.getElement().setAttribute("id", "matyNV");
        maty.addClickListener(event -> {
            if(Broadcaster.isMatyStart()) {
                maty.addClassName("highlight");
                mainView.getStyle().set("display", "none"); //rendi nuovamente visibile
                if (settingsView != null) {
                    settingsView.getStyle().set("display", "none");
                }
                if (gestStudentiView != null) {
                    gestStudentiView.getStyle().set("display", "none");
                }
                if(guessView != null){
                    guessView.getStyle().set("display", "none");
                }
                if (matyView == null) {
                    matyView = new MatyUI(endGamePublisher);
                    add(matyView);
                } else {
                    matyView.getStyle().set("display", "flex");
                }
                home.removeClassName("highlight");
                gestStud.removeClassName("highlight");
                settings.removeClassName("highlight");
                guess.removeClassName("highlight");
            }else{
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEventForTeacher("La partita non e' iniziata! Premi 'Avvia' in Gestione Studenti", "black", "420px");
            }
        });
        newGame = addDivInAccordionPanelContent("NuovoGioco", null, "64px");
        newGame.getElement().setAttribute("id", "newGameNV");
        newGame.addClickListener(event ->{
            InfoEventUtility infoEventUtility = new InfoEventUtility();
            infoEventUtility.infoEventForTeacher("Coming soon...", "green", "");
        });
        panel1.add(guess, maty, newGame);
        Div gamesItem = addDivInAccordionPanelContent("Giochi", ic, "0px");
        gamesItem.getStyle().set("width", "230px");
        accordion.add(new AccordionPanel(gamesItem, panel1)).addThemeVariants(DetailsVariant.REVERSE);

        Icon logoutIcon = new Icon(VaadinIcon.SIGN_OUT);
        logoutIcon.setSize(icon_Size);
        logoutIcon.setColor(icon_color);
        Div logout = addDivInAccordionPanelContent("Logout", logoutIcon, "-16px");
        logout.getStyle().set("width", "236px");
        logout.getStyle().set("margin-left", "-4px");
        logout.addClickListener(event -> {
            VaadinSession.getCurrent().getSession().invalidate();  //chiudi la sessione utente corrente
            UI.getCurrent().navigate(MainView.class);  //vai alla pagina "MainView" (classe con @Route("MainView")
            UI.getCurrent().getPage().reload();
        });
        accordion.add(new AccordionPanel(logout, new Div()));

        d.add(accordion);
        return d;
    }

    private Div addDivInAccordionPanelContent(String linkText, Icon ic, String marginleft){
        Div d = new Div();
        d.addClassName("navi-item");
        d.getStyle().set("cursor", "pointer");
        d.getStyle().set("width", NAVBAR_WIDTH);
        RouterLink rl = new RouterLink();
        rl.addClassName("navi-item__link_logout");
        rl.getStyle().set("margin-left", marginleft);
        Span sp = new Span(linkText);
        if(ic != null)
            rl.add(ic, sp);
        else
            rl.add(sp);
        d.add(rl);
        return d;
    }

    private Div addLogoContainer(){
        Div d = new Div();
        d.addClassName("brand-expression");
        d.getStyle().set("width", "100%");
        Image img = new Image("icon.png", "ConnectTeam logo");
        img.getStyle().set("max-height", "100%");
        img.getStyle().set("max-width", "100%");
        Label lab = new Label("ConnecTeam");
        lab.addClassNames("custom_h3", "brand-expression__title");
        d.add(img, lab);
        return d;
    }

    //public methods
    public void resetGestioneStudentUI(boolean isResetAll){
        UI.getCurrent().getPage().executeJs("document.getElementById(\"homeNV\").click();");
        if(isResetAll) { //se true, rimuovi anche l'istanza "GestioneStudentUI
            if (gestStudentiView != null) {
                remove(gestStudentiView);
                gestStudentiView = null;
            }
            if (gestStudentiView != null) {
                remove(gestStudentiView);
                gestStudentiView = null;
            }
        }//else vai solo alla Home
    }

    //Implements method of BeforeLeaveObserver interface
    @Override
    public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {
        if(isShowErrorDialog)
            return; //Se viene mostrato il dialog di errore -> esci da questo metodo

        if(Broadcaster.isGuessStart()){  //Una partita di Guess e' in corso?
            BroadcasterGuess.terminaPartitaForAll("Partita terminata!! Teacher si e' disconnesso");
        }
        if(Broadcaster.isMatyStart()){ //Una partita di Maty e' in corso?
            BroadcasterMaty.terminaPartitaForAll("Partita terminata!! Teacher si e' disconnesso");
        }

        Broadcaster.unregisterTeacher(account, this);
        Broadcaster.unregisterTeacherForGestStud(account); //inserito qui perche' non viene usata una pagina dedicata per GestioneStudentUI (cioe' UI.navigate)
        Broadcaster.removeAccountWithWebBrowser(account);
        Broadcaster.resetCounterUserGame(); //vincolato ad un solo teacher
        Broadcaster.resetFlagIsGameStart(); //necessario, altrimenti quando teacher effettua logout non vengono impostati a false
        Broadcaster.removeAccountWithWebBrowser(account);
        UI.getCurrent().setPollInterval(-1); //Quando teacher esce da questa pagina, disattiva il 'Poll'
    }

    //Implementazione 'BroadcasterListenerTeacher'
    @Override
    public void updateGridStudentCollegati(){
        //no implement
    }

    @Override
    public void removeAccountFromAllGrid(Account a){
        //no implement
    }

    //Inizializza istanza GuessUI/MatyUI quando teacher avvia la partita e rende la view dedicata non visibile
    @Override
    public void startGameInBackground(String game){
        //Verifica presenza di qualche eccezione
        try{
            if(!getUI().isPresent())
                throw new NoSuchElementException("TeacherMainUITab.startGameInBackground(): No value present in getUI()");
        }catch(NoSuchElementException e){
            e.printStackTrace();
        }

        if(game.equals("Guess")){
            getUI().get().accessSynchronously(()->{
                guessView = new GuessUI(endGamePublisher);
                add(guessView);
                guessView.getStyle().set("display", "none");
            });
        }else if(game.equals("Maty")){
            getUI().get().accessSynchronously(()->{
                matyView = new MatyUI(endGamePublisher);
                add(matyView);
                matyView.getStyle().set("display", "none");
            });
        }
    }

    @Override
    public void configFinePartitaTeacher(String nameGame, Gruppo g, String statusPartita){

        while(!getUI().isPresent()){
            System.out.println("TeacherMainUITab.configFinePartitaTeacher(): getUI() is Not present ");
        } //attendi la UI
        getUI().get().access(() -> {
            if(statusPartita.equals("")) {
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.partitaTerminataDialogTeacher(nameGame, "Partita Terminata! Non risultano altri utenti connessi");
            }else if(statusPartita.equals("vincente")){
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEventForTeacher("Il " + g.getId() + " ha vinto la partita!!", "green", "");
            }else if(statusPartita.equals("non-vincente")){
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEventForTeacher("Il " + g.getId() + " non ha vinto la partita!!", "red", "");
            }
            resetGestioneStudentUI(true);
        });

    }
}
