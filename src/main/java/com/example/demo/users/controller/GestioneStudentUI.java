package com.example.demo.users.controller;

import com.example.demo.entity.Gruppo;
import com.example.demo.users.broadcaster.BroadcastListenerTeacher;
import com.example.demo.users.broadcaster.Broadcaster;
import com.example.demo.entity.Account;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.userOperation.NavBarVertical;
import com.example.demo.users.event.StartGameEventBeanPublisher;
import com.example.demo.utility.AppBarUI;
import com.example.demo.utility.InfoEventUtility;
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
import com.vaadin.flow.component.tabs.Tab;
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
    private List<Grid<Account>> gridGruppi;
    private List<Gruppo> gruppi;
    private Label title;
    private HorizontalLayout gridContainer;
    private AppBarUI appBarUI;

    public GestioneStudentUI(/*@Autowired*/ StartGameEventBeanPublisher startGameEventPublisher){

        try {
            //Inizializzazione
            accRep = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
            account = (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
            startGameEventBeanPublisher = startGameEventPublisher;
            gridGruppi = new ArrayList<Grid<Account>>();
            gruppi = new ArrayList<Gruppo>();
            setId("GestioneStudentUI"); //setta id del root element di questo component

            //Registra un teacher listener
            Broadcaster.registerTeacherForGestStud(account, this);

            showSettingsDialog();

            getStyle().set("height", "100%"); //per nav bar verticale
            getStyle().set("width", "100%");
            appBarUI = new AppBarUI("Gestione studenti", false, false); //nome pagina corrente
            add(appBarUI);
            showButtonInAppBar();

            VerticalLayout containerTitleGuide = new VerticalLayout();
            containerTitleGuide.setPadding(false);
            containerTitleGuide.setSpacing(false);
            containerTitleGuide.addClassName("containerTitleGuide");
            containerTitleGuide.setWidth("80%");
            containerTitleGuide.getStyle().set("left", NavBarVertical.NAVBAR_WIDTH);

            title = new Label("Gestione gruppi");
            title.addClassName("titleStyle");

            Paragraph guidetxt = new Paragraph("Selezionate uno studente dalla lista e trascinatelo nel gioco che desiderate." +
                    "Premere il pulsante 'Gioca' per iniziare una nuova partita con il gruppo di studenti che è stato impostato.");

            containerTitleGuide.add(title, guidetxt);
            add(containerTitleGuide);

            gridContainer = new HorizontalLayout();
            gridContainer.addClassName("gridContainer");
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
            if(nomeGioco.equals("NuovoGioco")){
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEventForTeacher("Coming soon...", "green", "");
            }else{
                startGame();
            }
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
        selects.setItems("Guess", "Maty", "NuovoGioco");
        selects.setValue("Guess");

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
        b.addClickListener(buttonClickEvent -> {
            nomeGioco = selects.getValue();
            numeroGruppi = new Double(numberField.getValue()).intValue();
            getStyle().set("display", "flex");

            title.setText(nomeGioco);

            VerticalLayout contStud = containerListStudent();
            gridContainer.add(contStud);

            createGridsAndGroups();
            configurationGridDragAndDrop();

            d.close();
        });

        Button close = new Button("Chiudi");
        close.addClickListener(buttonClickEvent -> {
            getStyle().set("display", "none");
            d.close();
        });
        btnContainer.add(b, close);

        content.add(titleDialog, hor1, hor2, btnContainer);
        d.add(content);
        d.open();
    }

    private void configurationGridDragAndDrop(){

        ComponentEventListener<GridDragStartEvent<Account>> dragStartListener = event -> {
            draggedItems = event.getDraggedItems();
            dragSource = event.getSource();
            gridStud.setDropMode(GridDropMode.BETWEEN); //imposta modalita' di trascinamento delle righe; GridDropMode.BETWEEN: il drop event si verifica tra righe della Grid
            for(int i = 0; i < gridGruppi.size(); i++){
                gridGruppi.get(i).setDropMode(GridDropMode.BETWEEN);
            }

        };

        ComponentEventListener<GridDragEndEvent<Account>> dragEndListener = event -> {
            draggedItems = null;
            dragSource = null;
            gridStud.setDropMode(null);
            for(int i = 0; i < gridGruppi.size(); i++){
                gridGruppi.get(i).setDropMode(null);
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

        gridStud.setSelectionMode(Grid.SelectionMode.NONE); //non mostra checkbox per selezionare la riga
        gridStud.addDropListener(dropListener);
        gridStud.addDragStartListener(dragStartListener);
        gridStud.addDragEndListener(dragEndListener);
        gridStud.setRowsDraggable(true); //utente può usare Drag-and-Drop delle righe

        for(int i = 0; i < gridGruppi.size(); i++){
            gridGruppi.get(i).setSelectionMode(Grid.SelectionMode.NONE);
            gridGruppi.get(i).addDropListener(dropListener);
            gridGruppi.get(i).addDragStartListener(dragStartListener);
            gridGruppi.get(i).addDragEndListener(dragEndListener);
            gridGruppi.get(i).setRowsDraggable(true);
        }

    }

    private void createGridsAndGroups(){

        Tabs tabs = new Tabs();
        tabs.setWidth("75%");
        tabs.getStyle().set("padding", "0");

        for(int i = 0, j=i+1; i < numeroGruppi; i++, j++){
            Grid<Account> grid = new Grid<>(Account.class);
            grid.removeAllColumns();
            grid.getElement().setAttribute("id", "Gruppo" + j);
            grid.addColumn(Account::getNome).setHeader("Gruppo " + j);
            grid.getStyle().set("width", "100%");
            grid.getStyle().set("height", "80%");
            gridGruppi.add(grid);

            Gruppo g = new Gruppo();
            g.setId("Gruppo "+j);
            g.setNomeGioco(nomeGioco);
            gruppi.add(g);

            VerticalLayout vert = new VerticalLayout();
            vert.getElement().setAttribute("id", "gridContainer"+j);
            vert.addClassName("gridContainerVertLayout");
            vert.setSpacing(false);
            vert.add(gridGruppi.get(i));
            vert.setWidth("230px"); //default: 100%
            vert.setHeight("100%");

            Tab t = new Tab(vert);
            t.getElement().setAttribute("id","Gruppo" + j);
            t.getStyle().set("padding", "0");
            tabs.add(t);

        }
        gridContainer.add(tabs);
        add(gridContainer);
    }

    private VerticalLayout containerListStudent(){
        VerticalLayout vert = new VerticalLayout();
        vert.addClassName("gridContainerVertLayout");
        vert.setSpacing(false);
        vert.getStyle().set("margin-right", "16px");
        vert.setWidth("650px"); //default: 250px o 100%

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
        for(int i = 0; i < gridGruppi.size(); i++){
            ListDataProvider<Account> sourceDataProvider = (ListDataProvider<Account>) gridGruppi.get(i).getDataProvider();
            List<Account> sourceItems = new ArrayList<>(sourceDataProvider.getItems());
            gruppi.get(i).setMembri(sourceItems);
        }

        updateCountofGuessAndMatyUser();
        if(countGuessUser > 1 && nomeGioco.equals("Guess")){ //vincolo di almeno due utenti inseriti nella grid
            //invia un event contenente la lista di studenti collegati che devono giocare con Guess
            startGameEventBeanPublisher.doStuffAndPublishAnEvent(gruppi, nomeGioco);
            Broadcaster.setIsGuessStart(true);
        }else if(countMatyUser > 1 && nomeGioco.equals("Maty")){ //vincolo di almeno due utenti inseriti nella grid
            //invia un event contenente la lista di studenti collegati che devono giocare con Guess
            startGameEventBeanPublisher.doStuffAndPublishAnEvent(gruppi, nomeGioco);
            Broadcaster.setIsMatyStart(true);
        }else{
            InfoEventUtility infoEventUtility = new InfoEventUtility();
            infoEventUtility.infoEventForTeacher("Almeno due utenti per poter iniziare la partita", "black", "330px");
        }

    }

    private void updateCountofGuessAndMatyUser(){
        countGuessUser = 0;
        countMatyUser = 0;

        for(int i = 0; i < gruppi.size(); i++){
            if(gruppi.get(i).getNomeGioco().equals("Guess")){
                for(Account a : gruppi.get(i).getMembri()){
                    countGuessUser++;
                }
            }else if(gruppi.get(i).getNomeGioco().equals("Maty")) {
                for (Account a : gruppi.get(i).getMembri()) {
                    countMatyUser++;
                }
            }
        }
    }

    //Implementazione 'BroadcastListenerTeacher'
    @Override
    public void startGameInBackground(String game){
        //no implement
    }

    @Override
    public void showDialogFinePartitaTeacher(String nameGame){
        //No implemeny
    }

    @Override
    //Aggiorna 'Grid' Studenti collegati (rimuove dalla lista gli account gia' presenti nelle altre grid)
    public void updateGridStudentCollegati(){
        Map<Account, String> actualList = Broadcaster.getAccountListReceive();
        for(Account i : actualList.keySet()){
            for(Gruppo g : gruppi){
                for(Account x : g.getMembri()){
                    if(x.equals(i)){
                        actualList.remove(i);
                    }
                }
            }
        }

        if(getUI().isPresent()) {  //se e' presente una UI attached a questo component
            getUI().get().access(() -> {
                gridStud.setItems(actualList.keySet());
            });
        }else{
            gridStud.setItems(actualList.keySet());
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

                for(int i = 0; i < gridGruppi.size(); i++){
                    sourceDataProvider = (ListDataProvider<Account>) gridGruppi.get(i).getDataProvider();
                    sourceItems = new ArrayList<>(sourceDataProvider.getItems());
                    if(sourceItems.contains(a)) {
                        sourceItems.remove(a);
                        gridGruppi.get(i).setItems(sourceItems);
                        gruppi.get(i).setMembri(sourceItems);
                    }
                }

            });
        }
    }

}
