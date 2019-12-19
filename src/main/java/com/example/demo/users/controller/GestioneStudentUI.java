package com.example.demo.users.controller;

import com.example.demo.entity.Gruppo;
import com.example.demo.users.broadcaster.BroadcastListenerTeacher;
import com.example.demo.users.broadcaster.Broadcaster;
import com.example.demo.entity.Account;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.users.event.StartGameEventBeanPublisher;
import com.example.demo.utility.AppBarUI;
import com.example.demo.utility.InfoEventUtility;
import com.example.demo.utility.Utils;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import java.util.*;

@Push
@Route("ControllerGestStud")
@HtmlImport("style.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/gestStudStyle.css")
@PageTitle("Gestione Studenti")
public class GestioneStudentUI extends HorizontalLayout implements BroadcastListenerTeacher {

    //costanti
    private static final int MAX_NUM_GRUPPI = 10;
    private static final String[] SELECTS_ITEM = {"Guess", "Maty", "Nuovo gioco"};

    //instance field
    private AccountRepository accRep;
    private Account account;
    private Grid<Account> gridStud = new Grid<>(Account.class);
    private List<Account> draggedItems; //elementi soggetti a DnD
    private Grid<Account> dragSource; //sorgente da cui 'parte' il DnD
    private StartGameEventBeanPublisher startGameEventBeanPublisher;
    private int countGuessUser = 0;
    private int countMatyUser = 0;
    private int numeroGruppi = 0; //numeri di gruppi di creare
    private String nomeGioco = ""; //nome del gioco che si sta gestendo
    private List<Grid<Account>> gridGruppiGuess;
    private List<Grid<Account>> gridGruppiMaty;
    private List<Gruppo> gruppiGuess;
    private List<Gruppo> gruppiMaty;
    private HorizontalLayout gridContainer;
    private AppBarUI appBarUI;
    private HorizontalLayout mainGuess;
    private HorizontalLayout mainMaty;
    private GestioneStudentUIGuess gestioneStudentUIGuess;
    private GestioneStudentUIMaty gestioneStudentUIMaty;
    private Tabs containerGridGuess;
    private Tabs containerGridMaty;
    private ArrayList<String> selectsItem;
    private boolean isGridContainerAddToUI;
    private boolean isGridStudConfigurated; //gridstud e' stato configurato almeno una volta
    private String currentGameShow = ""; //UI che in questo momento sta visualizzando il teacher

    public GestioneStudentUI(StartGameEventBeanPublisher startGameEventPublisher, AccountRepository accRep, Account account){

        try {
            //Inizializzazione
            this.accRep = accRep;
            this.account = account;
            startGameEventBeanPublisher = startGameEventPublisher;
            setId("GestioneStudentUI"); //setta id del root element di questo component
            gridGruppiGuess = new ArrayList<Grid<Account>>();
            gridGruppiMaty = new ArrayList<Grid<Account>>();
            gruppiGuess = new ArrayList<Gruppo>();
            gruppiMaty = new ArrayList<Gruppo>();
            containerGridGuess = new Tabs();
            containerGridMaty = new Tabs();
            isGridContainerAddToUI = false;
            isGridStudConfigurated = false;

            //Inizializza list string items for Selects
            selectsItem = new ArrayList<String>();
            for(String str : SELECTS_ITEM){
                selectsItem.add(str);
            }

            //Registra un teacher listener
            Broadcaster.registerTeacherForGestStud(account, this);

            showSettingsDialog();

            getStyle().set("height", "100%"); //per nav bar verticale
            getStyle().set("width", "100%");
            appBarUI = new AppBarUI("Gestione studenti", false, false); //nome pagina corrente
            add(appBarUI);
            showButtonInAppBar();

            //Inizializza main layout di GestioneStudentiUI per guess e maty e all'inizio imposta come invisibili
            gestioneStudentUIGuess = new GestioneStudentUIGuess(this);
            mainGuess = new HorizontalLayout();
            mainGuess.getElement().setAttribute("id", "mainGuess");
            mainGuess.getStyle().set("display", "none");
            mainGuess.add(gestioneStudentUIGuess);  //non include 'gridContainer' -> usare 'nomeGioco' = Guess

            gestioneStudentUIMaty = new GestioneStudentUIMaty(this);
            mainMaty = new HorizontalLayout();
            mainMaty.getElement().setAttribute("id", "mainMaty");
            mainMaty.getStyle().set("display", "none");
            mainMaty.add(gestioneStudentUIMaty);  //non include 'gridContainer' -> usare 'nomeGioco' = Maty

            add(mainGuess, mainMaty);

            gridContainer = new HorizontalLayout();
            gridContainer.addClassName("gridContainer");
            gridContainer.getStyle().set("margin-top", "200px"); //valore precedente: 180px
            gridContainer.setWidth("75%");

            updateGridStudentCollegati();

            //UI.getCurrent().setPollInterval(5000); Da usare solo le pagina viene caricata con UI.navigate(...)
        }catch (Exception e){
            removeAll();
            getStyle().set("background-color","white");
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
            e.printStackTrace();
        }

    }

    private void showButtonInAppBar(){
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.getStyle().set("position", "absolute");
        horizontalLayout.getStyle().set("left", "75%");
        horizontalLayout.getStyle().set("z-index", "2");
        horizontalLayout.setHeight(AppBarUI.APPBAR_HEIGHT);

        Icon start = new Icon(VaadinIcon.PLAY);
        start.setSize(AppBarUI.ICON_BTN_SIZE);
        Button startBtn = new Button("Inizia partita", start);
        startBtn.setHeight(AppBarUI.APPBAR_HEIGHT);
        startBtn.getStyle().set("background-color", "#0000");
        startBtn.getStyle().set("margin", "0");
        startBtn.addClickListener(buttonClickEvent -> {
            if(currentGameShow.equals("Guess") && Broadcaster.isGuessStart()){
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEventForTeacher("Partita gia' iniziata!! Attendere la fine della partita o terminarla", "red", "450px");
            }else if(currentGameShow.equals("Maty") && Broadcaster.isGuessStart()){
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEventForTeacher("Partita gia' iniziata!! Attendere la fine della partita o terminarla", "red", "450px");
            }
            startGame();
        });

        Icon settings = new Icon(VaadinIcon.COG_O);
        settings.setSize(AppBarUI.ICON_BTN_SIZE);
        Button settingsBtn = new Button("Impostazioni", settings);
        settingsBtn.setHeight(AppBarUI.APPBAR_HEIGHT);
        settingsBtn.getStyle().set("background-color", "#0000");
        settingsBtn.getStyle().set("margin", "0");
        settingsBtn.addClickListener(buttonClickEvent -> {
            showSettingsDialog();
        });

        horizontalLayout.add(startBtn, settingsBtn);
        appBarUI.add(horizontalLayout);
    }

    private void showSettingsDialog(){
        Dialog d = new Dialog();
        d.setWidth("100%");
        d.setHeight("100%");
        d.setCloseOnOutsideClick(false);
        d.setCloseOnEsc(false);

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);

        Label titleDialog = new Label("Gestione gruppi");
        titleDialog.addClassName("titleDialog");

        HorizontalLayout hor1 = new HorizontalLayout();
        hor1.getStyle().set("margin-top", "16px");
        Label lab1 = new Label("Seleziona gioco: ");
        lab1.addClassName("dialogLabel1");

        Select<String> selects = new Select<>();
        selects.setItems(selectsItem);
        if(selectsItem.size() > 0){
            selects.setValue(selectsItem.get(0));
        }

        hor1.add(lab1, selects);

        HorizontalLayout hor2 = new HorizontalLayout();
        hor2.getStyle().set("margin-top", "16px");
        Label lab2 = new Label("Quanti gruppi?");
        lab2.addClassName("dialogLabel2");
        NumberField numberField = new NumberField();
        numberField.setValue(1d);
        numberField.setHasControls(true);
        numberField.setMin(1);
        numberField.setMax(MAX_NUM_GRUPPI);

        hor2.add(lab2, numberField);

        HorizontalLayout btnContainer = new HorizontalLayout();
        btnContainer.addClassName("btnContainerDialog");
        Button b = new Button("OK");
        b.setDisableOnClick(true); //un solo click alla volta e' ammesso, viene riattivato quando si chiude il dialog
        b.addClickListener(buttonClickEvent -> {
            nomeGioco = selects.getValue();

            if(nomeGioco.equals("Nuovo gioco")){
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEventForTeacher("Coming soon...", "green", "");
                buttonClickEvent.getSource().setEnabled(true);
                return;
            }

            currentGameShow = nomeGioco;

            //Una volta settate le impostazioni per un item della selects, tale item viene rimosso
            selectsItem.remove(nomeGioco);
            selects.setItems(selectsItem);

            numeroGruppi = new Double(numberField.getValue()).intValue();
            if(numeroGruppi < 1 || numeroGruppi > MAX_NUM_GRUPPI){
                buttonClickEvent.getSource().setEnabled(true);
                return;
            }

            if(!isGridContainerAddToUI){
                VerticalLayout contStud = containerListStudent();
                gridContainer.add(contStud);  //aggiungi solo una volta grid 'Studenti collegati', essa vale per tutte le schermate giochi

                add(gridContainer);
                isGridContainerAddToUI = true;
            }

            if(currentGameShow.equals("Guess")){
                setConfigurationForGuess();
            }else if(currentGameShow.equals("Maty")){
                setConfigurationForMaty();
            }

            configurationGridDragAndDrop();

            d.close();
            buttonClickEvent.getSource().setEnabled(true);
        });

        Button close = new Button("Chiudi");
        close.addClickListener(buttonClickEvent -> {
            d.close();
        });
        btnContainer.add(b, close);

        content.add(titleDialog, hor1, hor2, btnContainer);
        d.add(content);
        d.open();
    }

    public void setConfigurationForGuess(){
        mainGuess.getElement().setAttribute("name", "createdGuess");

        gestioneStudentUIGuess.setNumeroGruppi(numeroGruppi);
        gestioneStudentUIGuess.setNomeGioco(nomeGioco);
        gestioneStudentUIGuess.setTitleLabel(nomeGioco);

        containerGridMaty.getStyle().set("display", "none");
        containerGridGuess = gestioneStudentUIGuess.createGridsAndGroups();
        containerGridGuess.getStyle().set("display", "flex");

        gridGruppiGuess = gestioneStudentUIGuess.getGridGruppi();
        gruppiGuess = gestioneStudentUIGuess.getGruppi();

        gridContainer.add(containerGridGuess); //aggiunge un Tabs con id = 'tabsGUESS'

        gestioneStudentUIGuess.showBtnForChangeUI();

        mainGuess.getStyle().set("display", "flex");
        mainMaty.getStyle().set("display", "none");
    }

    public void setConfigurationForMaty(){
        mainMaty.getElement().setAttribute("name", "createdMaty");

        gestioneStudentUIMaty.setNumeroGruppi(numeroGruppi);
        gestioneStudentUIMaty.setNomeGioco(nomeGioco);
        gestioneStudentUIMaty.setTitleLabel(nomeGioco);

        containerGridGuess.getStyle().set("display", "none");
        containerGridMaty = gestioneStudentUIMaty.createGridsAndGroups();
        containerGridMaty.getStyle().set("display", "flex");

        gridGruppiMaty = gestioneStudentUIMaty.getGridGruppi();
        gruppiMaty = gestioneStudentUIMaty.getGruppi();

        gridContainer.add(containerGridMaty); //aggiunge un Tabs con id = 'tabsMATY'

        gestioneStudentUIMaty.showBtnForChangeUI();

        mainMaty.getStyle().set("display", "flex");
        mainGuess.getStyle().set("display", "none");
    }

    private void configurationGridDragAndDrop(){

        ComponentEventListener<GridDragStartEvent<Account>> dragStartListener = event -> {
            draggedItems = event.getDraggedItems();
            dragSource = event.getSource();
            gridStud.setDropMode(GridDropMode.BETWEEN); //imposta modalita' di trascinamento delle righe; GridDropMode.BETWEEN: il drop event si verifica tra righe della Grid

            for(int i = 0; i < gridGruppiGuess.size(); i++){
                gridGruppiGuess.get(i).setDropMode(GridDropMode.BETWEEN);
            }

            for(int i = 0; i < gridGruppiMaty.size(); i++){
                gridGruppiMaty.get(i).setDropMode(GridDropMode.BETWEEN);
            }
        };

        ComponentEventListener<GridDragEndEvent<Account>> dragEndListener = event -> {
            draggedItems = null;
            dragSource = null;
            gridStud.setDropMode(null);

            for(int i = 0; i < gridGruppiGuess.size(); i++){
                    gridGruppiGuess.get(i).setDropMode(null);
            }

            for(int i = 0; i < gridGruppiMaty.size(); i++){
                gridGruppiMaty.get(i).setDropMode(null);
            }
        };

        ComponentEventListener<GridDropEvent<Account>> dropListener = event -> {
            Optional<Account> target = event.getDropTargetItem();
            if (target.isPresent() && draggedItems.contains(target.get())) {
                return;
            }

            // Remove the items from the source grid
            @SuppressWarnings("unchecked")
            ListDataProvider<Account> sourceDataProvider = (ListDataProvider<Account>) dragSource.getDataProvider();
            List<Account> sourceItems = new ArrayList<>(sourceDataProvider.getItems());
            sourceItems.removeAll(draggedItems);
            dragSource.setItems(sourceItems);

            // Add dragged items to the target Grid
            Grid<Account> targetGrid = event.getSource();
            @SuppressWarnings("unchecked")
            ListDataProvider<Account> targetDataProvider = (ListDataProvider<Account>) targetGrid.getDataProvider();
            List<Account> targetItems = new ArrayList<>(targetDataProvider.getItems());

            int index = target.map(nome -> targetItems.indexOf(nome)
                    + (event.getDropLocation() == GridDropLocation.BELOW ? 1: 0)).orElse(0);
            targetItems.addAll(index, draggedItems);
            targetGrid.setItems(targetItems);
        };

        if(!isGridStudConfigurated){
            gridStud.setSelectionMode(Grid.SelectionMode.NONE); //non mostra checkbox per selezionare la riga
            gridStud.addDropListener(dropListener);
            gridStud.addDragStartListener(dragStartListener);
            gridStud.addDragEndListener(dragEndListener);
            gridStud.setRowsDraggable(true); //utente può usare Drag-and-Drop delle righe
            isGridStudConfigurated = true;
        }

        if(currentGameShow.equals("Guess")) {
            for (int i = 0; i < gridGruppiGuess.size(); i++) {
                gridGruppiGuess.get(i).setSelectionMode(Grid.SelectionMode.NONE);
                gridGruppiGuess.get(i).addDropListener(dropListener);
                gridGruppiGuess.get(i).addDragStartListener(dragStartListener);
                gridGruppiGuess.get(i).addDragEndListener(dragEndListener);
                gridGruppiGuess.get(i).setRowsDraggable(true);
            }
        }else if(currentGameShow.equals("Maty")){
            for (int i = 0; i < gridGruppiMaty.size(); i++) {
                gridGruppiMaty.get(i).setSelectionMode(Grid.SelectionMode.NONE);
                gridGruppiMaty.get(i).addDropListener(dropListener);
                gridGruppiMaty.get(i).addDragStartListener(dragStartListener);
                gridGruppiMaty.get(i).addDragEndListener(dragEndListener);
                gridGruppiMaty.get(i).setRowsDraggable(true);
            }
        }
    }

    private VerticalLayout containerListStudent(){
        VerticalLayout vert = new VerticalLayout();
        vert.addClassName("gridContainerVertLayout");
        vert.setSpacing(false);
        vert.getStyle().set("margin-right", "16px");
        vert.setWidth("250px"); //precedente: 650px

        gridStud.getStyle().set("width", "100%");
        gridStud.getStyle().set("height", "80%");  //valore precedente: 70%
        gridStud.getStyle().set("text-align", "center");

        gridStud.removeAllColumns(); //inserito per rimuovere tutte le colonne di Account (bug)
        gridStud.setId("AccountList");
        gridStud.addColumn(Account::getNome).setHeader("Studenti collegati");

        vert.add(gridStud);
        return vert;
    }

    //Inizia la partita di Guess o Maty quando teacher preme 'Inizia partita'
    private void startGame(){

        //Riempi la lista dei membri di ogni gruppo
        if(currentGameShow.equals("Guess")){
            for(int i = 0; i < gridGruppiGuess.size(); i++){
                ListDataProvider<Account> sourceDataProvider = (ListDataProvider<Account>) gridGruppiGuess.get(i).getDataProvider();
                List<Account> sourceItems = new ArrayList<>(sourceDataProvider.getItems());
                gruppiGuess.get(i).setMembri(sourceItems);
            }
        }else if(currentGameShow.equals("Maty")){
            for(int i = 0; i < gridGruppiMaty.size(); i++){
                ListDataProvider<Account> sourceDataProvider = (ListDataProvider<Account>) gridGruppiMaty.get(i).getDataProvider();
                List<Account> sourceItems = new ArrayList<>(sourceDataProvider.getItems());
                gruppiMaty.get(i).setMembri(sourceItems);
            }
        }

        updateCountofGuessAndMatyUser();
        if(countGuessUser > 1 && currentGameShow.equals("Guess")){ //vincolo di almeno due utenti inseriti nella grid
            //invia un event contenente la lista di studenti collegati che devono giocare con Guess
            startGameEventBeanPublisher.doStuffAndPublishAnEvent(gruppiGuess, currentGameShow);
            Broadcaster.setIsGuessStart(true);
            containerGridGuess.setEnabled(false);   //Disabilita tutti i tab
        }else if(countMatyUser > 1 && currentGameShow.equals("Maty")){ //vincolo di almeno due utenti inseriti nella grid
            //invia un event contenente la lista di studenti collegati che devono giocare con Guess
            startGameEventBeanPublisher.doStuffAndPublishAnEvent(gruppiMaty, currentGameShow);
            Broadcaster.setIsMatyStart(true);
            containerGridMaty.setEnabled(false);    //Disabilita tutti i tab
        }else{
            InfoEventUtility infoEventUtility = new InfoEventUtility();
            infoEventUtility.infoEventForTeacher("Almeno due utenti per poter iniziare la partita", "black", "330px");
        }

    }

    private void updateCountofGuessAndMatyUser(){
        countGuessUser = 0;
        countMatyUser = 0;

        for(Gruppo i : gruppiGuess){
            for(Account a : i.getMembri()){
                countGuessUser++;
            }
        }

        for(Gruppo i : gruppiMaty){
            for(Account a : i.getMembri()){
                countMatyUser++;
            }
        }
    }

    //Getter and setter
    public HorizontalLayout getMainGuess() {
        return mainGuess;
    }

    public HorizontalLayout getMainMaty() {
        return mainMaty;
    }

    public Tabs getContainerGridGuess() {
        return containerGridGuess;
    }

    public Tabs getContainerGridMaty() {
        return containerGridMaty;
    }

    public GestioneStudentUIGuess getGestioneStudentUIGuess() {
        return gestioneStudentUIGuess;
    }

    public GestioneStudentUIMaty getGestioneStudentUIMaty() {
        return gestioneStudentUIMaty;
    }

    public String getCurrentGameShow() {
        return currentGameShow;
    }

    public void setCurrentGameShow(String currentGameShow) {
        this.currentGameShow = currentGameShow;
    }

    //Implementazione 'BroadcastListenerTeacher'
    @Override
    public void startGameInBackground(String game){
        //no implement
    }

    @Override
    public void configFinePartitaTeacher(String nameGame, Gruppo g, String statusPartita){
        //No implement
    }

    @Override
    //Aggiorna 'Grid' Studenti collegati (rimuove dalla lista gli account gia' presenti nelle altre grid)
    public void updateGridStudentCollegati(){
        Set<Account> actualList = Utils.cloneListAccountsWithoutDuplicate(Broadcaster.getAccountListReceive()); //clonare la lista per risolvere problemi di accessi concorrenti ad AccountListEvent
        for(Account i : actualList){
            for(Gruppo g : gruppiGuess){    //verifica se un account e' stato assegnato ad un gruppo
                for(Account x : g.getMembri()){
                    if(x.equals(i)){
                        actualList.remove(i);
                    }
                }
            }

            for(Gruppo g : gruppiMaty){ //verifica se un account e' stato assegnato ad un gruppo
                for(Account x : g.getMembri()){
                    if(x.equals(i)){
                        actualList.remove(i);
                    }
                }
            }
        }

        if(getUI().isPresent()) {  //se e' presente una UI attached a questo component
            getUI().get().access(() -> {
                gridStud.setItems(actualList);
            });
        }else{
            gridStud.setItems(actualList);
        }
    }

    @Override
    public void removeAccountFromAllGrid(Account a){

        if(getUI().isPresent()){
            getUI().get().access(() -> {
                ListDataProvider<Account> sourceDataProvider = (ListDataProvider<Account>) gridStud.getDataProvider();
                List<Account> sourceItems = new ArrayList<>(sourceDataProvider.getItems());
                sourceItems.remove(a);
                gridStud.setItems(sourceItems);

                for(int i = 0; i < gridGruppiGuess.size(); i++){
                    sourceDataProvider = (ListDataProvider<Account>) gridGruppiGuess.get(i).getDataProvider();
                    sourceItems = new ArrayList<>(sourceDataProvider.getItems());
                    if(sourceItems.contains(a)) {
                        sourceItems.remove(a);
                        gridGruppiGuess.get(i).setItems(sourceItems);
                        if(gruppiGuess.size() > 0)
                            gruppiGuess.get(i).setMembri(sourceItems);  //Quando termina partita per tutti: Exception: java.lang.IndexOutOfBoundsException: Index: 1, Size: 0
                    }
                }

                for(int i = 0; i < gridGruppiMaty.size(); i++){
                    sourceDataProvider = (ListDataProvider<Account>) gridGruppiMaty.get(i).getDataProvider();
                    sourceItems = new ArrayList<>(sourceDataProvider.getItems());
                    if(sourceItems.contains(a)) {
                        sourceItems.remove(a);
                        gridGruppiMaty.get(i).setItems(sourceItems);
                        if(gruppiMaty.size() > 0)
                            gruppiMaty.get(i).setMembri(sourceItems);
                    }
                }

            });
        }
    }

}
