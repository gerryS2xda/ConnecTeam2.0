package com.example.demo.games.maty.gameMenagement.frondend;


import com.example.demo.entity.Account;
import com.example.demo.entity.CronologiaNumeri;
import com.example.demo.entity.Gruppo;
import com.example.demo.error.ErrorPage;
import com.example.demo.games.maty.gameMenagement.backend.MatyController;
import com.example.demo.games.maty.gameMenagement.backend.broadcaster.BroadcasterMaty;
import com.example.demo.games.maty.gameMenagement.backend.broadcaster.BroadcasterSuggerisciMaty;
import com.example.demo.games.maty.gameMenagement.backend.db.ItemMaty;
import com.example.demo.games.maty.gameMenagement.backend.listeners.SuggerisciListenerMaty;
import com.example.demo.utility.Utils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import java.util.ArrayList;
import java.util.List;

@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/matyStyle.css")
@StyleSheet("frontend://stile/button.css")
@StyleSheet("frontend://stile/buttonsend1.scss")
@StyleSheet("frontend://stile/divbox.css")
@StyleSheet("frontend://stile/animation.css")
@JavaScript("frontend://js/script.js")
public class StartGameMatyUI extends HorizontalLayout implements SuggerisciListenerMaty {

    //costanti
    private static final String HTML_NUMERO_INSERITO = "<div class=\"box boxGrid\">" +
            "<div id=\"colorpad2\" class=\"circleSmall\"><p class=\"parag3\">" +
            "<span>[[item.numeroInserito]]</span></p></div></div>";

    //static field
    private static List<Grid<CronologiaNumeri>> cronologiaGrids = new ArrayList<Grid<CronologiaNumeri>>();
    private static boolean isCronologiaGridsSetted = false; //le liste delle grid sono state riempite al piu' una volta?

    //instance field
    private MatyController matyController;
    private boolean vincente;
    private boolean flag = false;
    private Label operazioneLabel;
    private Button sendNumeroBtn = new Button();
    private Account account;
    private IntegerField suggerisciNumero;
    private boolean isTeacher;
    private VerticalLayout numeroInseritoWithEliminaBtnContainer;
    private List<Gruppo> gruppi = new ArrayList<Gruppo>();
    private ArrayList<Div> containersBox; //container per il numero inserito (biglia con numero all'interno e si trova sotto "Il tuo numero e':")
    private ArrayList<Div> containersBoxTeacher;
    private ArrayList<Div> containersWrapper; //<div class='box1'> contiene html dedicato per la pallina che si muove sullo schermo
    private ArrayList<VerticalLayout> numeroInseritoVLList;  //contiene 'numeroInseritoVL: ball numero inserito + eliminaBtn'
    private ArrayList<VerticalLayout> numeroInseritoVLTeacherList; //contiene 'numeroInseritoVL: ball numero inserito + eliminaBtn' per teacher
    private VerticalLayout interactionContainer;
    private VerticalLayout cronologiaGridContainer;
    private HorizontalLayout cronologiaNumeriGridsContainer;
    private HorizontalLayout cronologiaNumeriGridsContainerTeacher;
    private Grid<CronologiaNumeri> currentGridStudent; //per lo studente per risolvere 'Cannot access state in VaadinSession or UI without locking the session.'
    private List<Grid<CronologiaNumeri>> cronologiaGridsListTeacher;


    public StartGameMatyUI(MatyController matyController, Account account, boolean isTeacher) {

        try {
            //Inizializzazione UI
            setId("StartGameMatyUI");
            setWidth("100%");
            setHeight("100%");

            //Inizializzazione instance field
            this.account = account;
            this.matyController = matyController;
            this.isTeacher = isTeacher;
            gruppi = com.example.demo.users.broadcaster.Broadcaster.getGruppiListReceive();
            containersBox = new ArrayList<Div>();
            containersBoxTeacher = new ArrayList<Div>();
            containersWrapper = new ArrayList<Div>();
            numeroInseritoVLList = new ArrayList<VerticalLayout>();
            numeroInseritoVLTeacherList = new ArrayList<VerticalLayout>();
            currentGridStudent = new Grid<>(CronologiaNumeri.class);
            cronologiaGridsListTeacher = new ArrayList<Grid<CronologiaNumeri>>();

            BroadcasterSuggerisciMaty.register(account, this);

            //interactionContainer: contiene 'operazioneLabel', textfield, 'sommaBtn/sottrazioneBtn', numero inserito
            interactionContainer = new VerticalLayout();
            interactionContainer.setPadding(false);
            interactionContainer.addClassName("interactionContainerStyle");
            interactionContainer.setWidth("500px");
            interactionContainer.setHeight("400px");
            riempiInteractionContainer();
            add(interactionContainer);

            //'cronologiaNumeriGridsContainer'
            cronologiaGridContainer = new VerticalLayout();
            cronologiaGridContainer.setPadding(false);
            cronologiaGridContainer.setWidth("500px");
            cronologiaGridContainer.setHeight("400px");
            cronologiaGridContainer.addClassName("cronologiaContainerStyle");

            Label cronologiaMosse = new Label("Cosa hanno fatto fino ad ora");
            cronologiaMosse.addClassName("cronologiaMosse");

            cronologiaNumeriGridsContainer = new HorizontalLayout();
            cronologiaNumeriGridsContainer.setWidth("500px");
            cronologiaNumeriGridsContainer.setHeight("300px");

            cronologiaNumeriGridsContainerTeacher = new HorizontalLayout();
            cronologiaNumeriGridsContainerTeacher.setWidth("500px");
            cronologiaNumeriGridsContainerTeacher.setHeight("300px");

            if(!isCronologiaGridsSetted) {
                createStaticListGridForCronologiaNumeri();
                isCronologiaGridsSetted = true;
            }

            initStaticListGridCronologiaNumeri();

            initArrayListsAndAddToMainContent(); //ArrayList usati per implementare la gestione dei gruppi, un container per ogni gruppo

            if(!isTeacher) {
                createCurrentGridAndAddToContainerForStudent();
                cronologiaGridContainer.add(cronologiaMosse, cronologiaNumeriGridsContainer);
            }else{
                createOrUpdateListGridForTeacherAndAddToContainer();
                cronologiaGridContainer.add(cronologiaMosse, cronologiaNumeriGridsContainerTeacher);
            }
            add(cronologiaGridContainer);

            BroadcasterSuggerisciMaty.refreshContent();
        } catch (Exception e) {
            removeAll();
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
            e.printStackTrace();
        }

    }

    private void riempiInteractionContainer(){
        operazioneLabel = new Label();
        operazioneLabel.addClassName("labelOperazione");
        interactionContainer.add(operazioneLabel);

        HorizontalLayout inputFieldAndBtn = new HorizontalLayout();
        inputFieldAndBtn.getElement().setAttribute("id", "inputFieldAndBtn");
        suggerisciNumero = new IntegerField();
        suggerisciNumero.setPlaceholder("Enter a number...");
        suggerisciNumero.setWidth("150px");
        suggerisciNumero.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);


        Div divC = new Div();  //container per il btn (vedi 'buttonsend1.scss)
        divC.addClassName("container11");
        sendNumeroBtn.setId("btn");
        sendNumeroBtn.addClassName("button12");
        sendNumeroBtn.getStyle().set("cursor", "pointer");
        divC.add(sendNumeroBtn);
        sendNumeroInserito(); //business logic of sendNumeroBtn click listener

        inputFieldAndBtn.add(suggerisciNumero, divC);
        interactionContainer.add(inputFieldAndBtn);
        
        numeroInseritoWithEliminaBtnContainer = new VerticalLayout();
        numeroInseritoWithEliminaBtnContainer.setId("numeroInseritoWithEliminaBtnContainer");
        numeroInseritoWithEliminaBtnContainer.setWidth("auto");
        interactionContainer.add(numeroInseritoWithEliminaBtnContainer);
    }

    private void initArrayListsAndAddToMainContent(){
        for(Gruppo x : gruppi){
            //Div class='box' -> pallina che viene creata quando viene inserito un numero
            Div d = new Div();
            d.addClassName("box");
            d.getElement().setAttribute("name", x.getId());
            containersBox.add(d);

            Div d3 = new Div();
            d3.addClassName("box");
            d3.getElement().setAttribute("name", x.getId());
            containersBoxTeacher.add(d3);

            //Div 'wrapper' ->  usato per mostrare la biglia in movimento (SOLO student)
            Div d2 = new Div();  //e' un container che contiene a sua volta il 'wrapper'
            d2.getElement().setAttribute("name", x.getId());
            d2.getStyle().set("display", "none");
            containersWrapper.add(d2);

            //HorizontalLayout numeroInserito con eliminaBtn
            VerticalLayout vert = new VerticalLayout();
            vert.getElement().setAttribute("name", x.getId());
            vert.addClassName("numeroInseritoVL");
            vert.getStyle().set("display", "none");
            numeroInseritoVLList.add(vert);
            numeroInseritoWithEliminaBtnContainer.add(vert);

            VerticalLayout vert2 = new VerticalLayout();
            vert2.getElement().setAttribute("id", "teacher");
            vert2.getElement().setAttribute("name", x.getId());
            vert2.addClassName("numeroInseritoVLTeacher");
            vert2.getStyle().set("display", "none");
            numeroInseritoVLTeacherList.add(vert2);
            numeroInseritoWithEliminaBtnContainer.add(vert2);
        }
    }

    public void setOperazione(){
       for (int i = 0; i < BroadcasterSuggerisciMaty.getItems().size(); i++) {
            try {
                ItemMaty item = BroadcasterSuggerisciMaty.getItems().get(i);
                String operazione = item.getOperazione();
                System.out.println("StartGameMatyUI.setoperazione(): items size: " + BroadcasterSuggerisciMaty.getItems().size() + " Item: " + operazione);
                if (operazione.equalsIgnoreCase("somma")) {
                    operazioneLabel.setText("Somma un numero");
                    sendNumeroBtn.setText("Somma");
                } else {
                    operazioneLabel.setText("Sottrai un numero");
                    sendNumeroBtn.setText("Sottrai");
                }
            } catch (Exception e) {
                System.out.println("StartGameMatyUI.setOperazione(): " + e.getMessage());
            }
       }

    }

    private void checkIfWin(Gruppo gruppo) {
        for (int i = 0; i < BroadcasterMaty.getPartiteThread().size(); i++) {
            if (i == 0) {
                flag = false;
            }

            int punteggio = 0;
            if (BroadcasterMaty.getIndiziRicevuti() == 0) {
                punteggio = 120;
            } else if (BroadcasterMaty.getIndiziRicevuti() == 1) {
                punteggio = 100;
            } else if (BroadcasterMaty.getIndiziRicevuti() == 2) {
                punteggio = 60;
            } else if (BroadcasterMaty.getIndiziRicevuti() == 3) {
                punteggio = 30;
            } else if (BroadcasterMaty.getIndiziRicevuti() == 4) {
                punteggio = 10;
            }
            vincente = matyController.partitaVincente("" + BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1),
            BroadcasterMaty.getItems().get(i));
            if (vincente == true && flag == false) {
                BroadcasterMaty.getPartiteThread().get(i).interrupt();
                BroadcasterMaty.getPartiteThread().get(i).stopTimer();
                BroadcasterMaty.partitaVincente(gruppo, "" + BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1)
                           ,punteggio);
            }

        }
    }

    //Send button: funzionamento di tutto il gioco quando viene premuto il pulsante 'Somma / Sottrai'
    private void sendNumeroInserito(){
        sendNumeroBtn.addClickListener(buttonClickEvent -> {
            if(isTeacher){
                Gruppo gruppo = Utils.findGruppoByName(gruppi, MatyUI.currentGroupSelect.getId());
                VerticalLayout numeroInseritoVL = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLTeacherList, "name", gruppo.getId());
                numeroInseritoVL.removeAll();
            }else{
                Gruppo gruppo1 = Utils.findGruppoByAccount(gruppi, account);
                VerticalLayout numeroInseritoVL1 = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLList, "name", gruppo1.getId());
                numeroInseritoVL1.removeAll();
            }

            getElement().executeJs("sends()");
            String mess = String.valueOf(suggerisciNumero.getValue());
            if (!mess.equals(BroadcasterMaty.getItems().get(0).getParola())) {
                if (!mess.equals(String.valueOf(suggerisciNumero.getEmptyValue()))) {
                    try {
                        Integer.parseInt(mess);
                        BroadcasterMaty.addContClick();
                        System.out.println("Clicked " + BroadcasterMaty.getContClick().size() + " times");

                        for (int i = 0; i < BroadcasterSuggerisciMaty.getItems().size(); i++) {
                            try {
                                String operazione = BroadcasterSuggerisciMaty.getItems().get(i).getOperazione();
                                if (operazione.equalsIgnoreCase("somma")) {
                                    operazioneSomma(i, mess, operazione);
                                } else {
                                    operazioneSottrazione(i, mess, operazione);
                                }

                            } catch (Exception e) {
                            }
                        }
                    }catch (Exception e){
                        suggerisciNumero.setValue(suggerisciNumero.getEmptyValue());
                    }
                }
            }else {
                suggerisciNumero.setValue(suggerisciNumero.getEmptyValue());
            }
        });
    }

    private void operazioneSomma(int i, String mess, String operazione){ //sendBtn = 'Somma'
        if (BroadcasterMaty.getIntegers().size() == 0) {
            BroadcasterMaty.addIntegers(Integer.parseInt(mess));
        } else {
            BroadcasterMaty.addIntegers(
                    BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1) + Integer.parseInt(mess));
        }

        Gruppo gruppo1 = new Gruppo();
        Gruppo gruppo2 = new Gruppo();

        if(isTeacher){
            gruppo1 = Utils.findGruppoByName(gruppi, MatyUI.currentGroupSelect.getId());
            BroadcasterSuggerisciMaty.broadcast(mess, operazione, true, account, gruppo1);
        }else{
            gruppo2 = Utils.findGruppoByAccount(gruppi, account);
            BroadcasterSuggerisciMaty.broadcast(mess, operazione, true, account, gruppo2);
        }

        /*----INIZIO------------------------------------------------------------------------------------------------------------------------------------*/
        int j;
        if (operazione.equals("somma")) {
            j = 0;
        } else {
            j = 1;
        }

        if(!isTeacher){
            riempiAndShowNumeroInseritoContainerStudent(mess, operazione, gruppo2);
        }else{
            riempiAndShowNumeroInseritoContainerTeacher(mess, operazione, gruppo1);
        }

        getElement().executeJs("setRandomColor()"); //associa un random color a 'backgroud-color' delle 'ball'

        /*--FINE--------------------------------------------------------------------------------------------------------------------------------------*/
        suggerisciNumero.setValue(suggerisciNumero.getEmptyValue());
        if(!isTeacher)  //per il teacher questo pulsante deve rimanere sempre attivo
            sendNumeroBtn.setEnabled(false);
        if (BroadcasterMaty.getContClick().size() == 5) {
            String indizio = BroadcasterSuggerisciMaty.getItems().get(i).getIndizio(0);
            BroadcasterMaty.riceveIndizio(indizio);
            BroadcasterMaty.setIndiziRicevuti(1);
        } else if (BroadcasterMaty.getContClick().size() == 10) {
            String indizio = BroadcasterSuggerisciMaty.getItems().get(i).getIndizio(1);
            BroadcasterMaty.riceveIndizio(indizio);
            BroadcasterMaty.setIndiziRicevuti(2);
        } else if (BroadcasterMaty.getContClick().size() == 15) {
            String indizio = BroadcasterSuggerisciMaty.getItems().get(i).getIndizio(2);
            BroadcasterMaty.riceveIndizio(indizio);
            BroadcasterMaty.setIndiziRicevuti(3);
        } else if (BroadcasterMaty.getContClick().size() == 20) {
            String indizio = BroadcasterSuggerisciMaty.getItems().get(i).getIndizio(3);
            BroadcasterMaty.riceveIndizio(indizio);
            BroadcasterMaty.setIndiziRicevuti(4);
        }
        checkIfWin(gruppo2); //verifica se il numero inserito conduce alla soluzione (SOLO student)
    }

    private void operazioneSottrazione(int i, String mess, String operazione){ //sendBtn = 'Sottrai'
        System.out.println(operazione);
        System.out.println(BroadcasterSuggerisciMaty.getItems().get(i).getParola());
        BroadcasterMaty.addIntegers(
                BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1) - Integer.parseInt(mess));

        Gruppo gruppo1 = new Gruppo();
        Gruppo gruppo2 = new Gruppo();

        if(isTeacher){
            gruppo1 = Utils.findGruppoByName(gruppi, MatyUI.currentGroupSelect.getId());
            BroadcasterSuggerisciMaty.broadcast(mess, operazione, true, account, gruppo1);
        }else{
            gruppo2 = Utils.findGruppoByAccount(gruppi, account);
            BroadcasterSuggerisciMaty.broadcast(mess, operazione, true, account, gruppo2);
        }

        /*------INIZIO----------------------------------------------------------------------------------------------------------------------------------*/
        int j;
        if (operazione.equals("somma")) {
            j = 0;
        } else {
            j = 1;
        }

        if(!isTeacher){
            riempiAndShowNumeroInseritoContainerStudent(mess, operazione, gruppo2);
        }else{
            riempiAndShowNumeroInseritoContainerTeacher(mess, operazione, gruppo1);
        }

        getElement().executeJs("setRandomColor()"); //associa un random color a 'backgroud-color' delle 'ball'

        /*------FINE----------------------------------------------------------------------------------------------------------------------------------*/


        suggerisciNumero.setValue(suggerisciNumero.getEmptyValue());
        if(!isTeacher)  //per il teacher questo pulsante deve rimanere sempre attivo
            sendNumeroBtn.setEnabled(false);
        if (BroadcasterMaty.getContClick().size() == 5) {
            String indizio = BroadcasterSuggerisciMaty.getItems().get(i).getIndizio(0);
            BroadcasterMaty.riceveIndizio(indizio);
            BroadcasterMaty.setIndiziRicevuti(1);
            System.out.println("Trovato");
        } else if (BroadcasterMaty.getContClick().size() == 10) {
            String indizio = BroadcasterSuggerisciMaty.getItems().get(i).getIndizio(1);
            BroadcasterMaty.riceveIndizio(indizio);
            BroadcasterMaty.setIndiziRicevuti(2);
            System.out.println("Trovato");
        } else if (BroadcasterMaty.getContClick().size() == 15) {
            String indizio = BroadcasterSuggerisciMaty.getItems().get(i).getIndizio(2);
            BroadcasterMaty.riceveIndizio(indizio);
            BroadcasterMaty.setIndiziRicevuti(3);
            System.out.println("Trovato");
        } else if (BroadcasterMaty.getContClick().size() == 20) {
            String indizio = BroadcasterSuggerisciMaty.getItems().get(i).getIndizio(3);
            BroadcasterMaty.riceveIndizio(indizio);
            BroadcasterMaty.setIndiziRicevuti(4);
            System.out.println("Trovato");
        }
        checkIfWin(gruppo2); //verifica se il numero inserito conduce alla soluzione (SOLO student)
    }

    private void addDivToMainContent(Div d, Gruppo g){
        if(d.getElement().getAttribute("id") != null){
            if(d.getElement().getAttribute("id").equals("teacher")){
                add(d); //verra' reso visibile in seguito
            }
        }else if(Utils.isAccountInThisGruppo(g, account)){
            d.getStyle().set("display", "block");
            add(d);
        }else{
            d.getStyle().set("display", "none");
        }
    }

    //public methods
    public void hideAllContainerForTeacher(){

        for(Div i : containersBoxTeacher){
            i.getStyle().set("display", "none");
        }

        for(VerticalLayout i : numeroInseritoVLTeacherList){
            i.getStyle().set("display", "none");
        }

        for(Grid<CronologiaNumeri> i : cronologiaGridsListTeacher){
            i.getStyle().set("display", "none");
        }

    }

    private void createStaticListGridForCronologiaNumeri(){
        for(int i = 0; i < gruppi.size(); i++){
            Gruppo g = gruppi.get(i);
            Grid<CronologiaNumeri> grid = new Grid<>(CronologiaNumeri.class);
            grid.getElement().setAttribute("name", g.getId());
            grid.removeAllColumns();
            grid.addColumn(CronologiaNumeri::getNomeAccount).setHeader(" ")
                .setTextAlign(ColumnTextAlign.CENTER);
            grid.addColumn(TemplateRenderer.<CronologiaNumeri>of(HTML_NUMERO_INSERITO)
                    .withProperty("numeroInserito", CronologiaNumeri::getNumeroInserito))
                    .setHeader("Numero attuale").setTextAlign(ColumnTextAlign.CENTER);;
            grid.addColumn(CronologiaNumeri::getNumeriEliminatiListWithString)
                    .setHeader("Numeri eliminati").setTextAlign(ColumnTextAlign.CENTER);;
            grid.setWidth("100%");
            grid.setHeight("100%");
            grid.getStyle().set("display", "none");

            grid.setSelectionMode(Grid.SelectionMode.NONE); //disabilita 'selezione' delle righe

            cronologiaGrids.add(grid);
        }
    }

    private void initStaticListGridCronologiaNumeri(){

        if(!isTeacher) {
            Gruppo currentGruppo = Utils.findGruppoByAccount(gruppi, account);
            Grid<CronologiaNumeri> currentGrid = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGrids, "name", currentGruppo.getId());

            ListDataProvider<CronologiaNumeri> sourceDataProvider = (ListDataProvider<CronologiaNumeri>) currentGrid.getDataProvider();
            List<CronologiaNumeri> sourceItems = new ArrayList<>(sourceDataProvider.getItems());

            for (Account x : currentGruppo.getMembri()) {
                CronologiaNumeri y = new CronologiaNumeri(account);
                if (!sourceItems.contains(y)) {
                    y.setGruppo(currentGruppo);
                    sourceItems.add(y);
                }
            }
            currentGrid.setItems(sourceItems);

        }else{
            for(Gruppo x : gruppi){  //Aggiungi account del teacher in tutte le grid
                Grid<CronologiaNumeri> currentGrid = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGrids, "name", x.getId());
                ListDataProvider<CronologiaNumeri> sourceDataProvider = (ListDataProvider<CronologiaNumeri>) currentGrid.getDataProvider();
                List<CronologiaNumeri> sourceItems = new ArrayList<>(sourceDataProvider.getItems());

                CronologiaNumeri y = new CronologiaNumeri(account); //teacher account
                y.setGruppo(x);
                sourceItems.add(y);

                currentGrid.setItems(sourceItems);
            }
        }
    }

    //Clonazione di un oggetto che viene prelevato da 'cronologiaGrids' (risolve 'Cannot access state in VaadinSession or UI without locking the session.'=
    //NOTA: Ogni update effettuato su 'currentGrid', dovra' essere inserito manualmente anche in 'cronologiaGrids'
    private void createCurrentGridAndAddToContainerForStudent(){

        Gruppo currentGruppo = Utils.findGruppoByAccount(gruppi, account);
        Grid<CronologiaNumeri> currentGrid = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGrids, "name", currentGruppo.getId());
        ListDataProvider<CronologiaNumeri> sourceDataProvider = (ListDataProvider<CronologiaNumeri>) currentGrid.getDataProvider();
        List<CronologiaNumeri> sourceItems = new ArrayList<>(sourceDataProvider.getItems());

        currentGridStudent.getElement().setAttribute("name", currentGrid.getElement().getAttribute("name"));
        currentGridStudent.removeAllColumns();
        currentGridStudent.addColumn(CronologiaNumeri::getNomeAccount)
                .setHeader(" ").setTextAlign(ColumnTextAlign.CENTER);;
        currentGridStudent.addColumn(TemplateRenderer.<CronologiaNumeri>of(HTML_NUMERO_INSERITO)
                .withProperty("numeroInserito", CronologiaNumeri::getNumeroInserito))
                .setHeader("Numero attuale").setTextAlign(ColumnTextAlign.CENTER);;
        currentGridStudent.addColumn(CronologiaNumeri::getNumeriEliminatiListWithString)
                .setHeader("Numeri eliminati").setTextAlign(ColumnTextAlign.CENTER);;
        currentGridStudent.setWidth("100%");
        currentGridStudent.setHeight("100%");
        currentGridStudent.setSelectionMode(Grid.SelectionMode.NONE); //disabilita 'selezione' delle righe

        currentGridStudent.setItems(sourceItems);
        cronologiaNumeriGridsContainer.add(currentGridStudent);
    }

    private void createOrUpdateListGridForTeacherAndAddToContainer(){

        for(Gruppo x : gruppi){
            Grid<CronologiaNumeri> currentGrid = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGrids, "name", x.getId());
            ListDataProvider<CronologiaNumeri> sourceDataProvider = (ListDataProvider<CronologiaNumeri>) currentGrid.getDataProvider();
            List<CronologiaNumeri> sourceItems = new ArrayList<>(sourceDataProvider.getItems());

            Grid<CronologiaNumeri> newGrid = new Grid<>(CronologiaNumeri.class);
            newGrid.getElement().setAttribute("name", currentGrid.getElement().getAttribute("name"));
            newGrid.removeAllColumns();
            newGrid.addColumn(CronologiaNumeri::getNomeAccount).setHeader(" ")
                    .setTextAlign(ColumnTextAlign.CENTER);
            newGrid.addColumn(TemplateRenderer.<CronologiaNumeri>of(HTML_NUMERO_INSERITO)
                    .withProperty("numeroInserito", CronologiaNumeri::getNumeroInserito))
                    .setHeader("Numero attuale").setTextAlign(ColumnTextAlign.CENTER);;
            newGrid.addColumn(CronologiaNumeri::getNumeriEliminatiListWithString)
                    .setHeader("Numeri eliminati").setTextAlign(ColumnTextAlign.CENTER);;
            newGrid.setWidth("100%");
            newGrid.setHeight("100%");
            newGrid.getStyle().set("display", "none");
            newGrid.setSelectionMode(Grid.SelectionMode.NONE); //disabilita 'selezione' delle righe
            newGrid.setItems(sourceItems);

            cronologiaGridsListTeacher.add(newGrid);
            cronologiaNumeriGridsContainerTeacher.add(newGrid);
        }

    }

    private void updateAllGrid(String numeroInserito, String numeroEliminato, Account acc, Gruppo g){

            if (!isTeacher) {
                Grid<CronologiaNumeri> currentGrid = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGrids, "name", g.getId());
                ListDataProvider<CronologiaNumeri> sourceDataProvider = (ListDataProvider<CronologiaNumeri>) currentGrid.getDataProvider();
                List<CronologiaNumeri> sourceItems = new ArrayList<>(sourceDataProvider.getItems());

                if(sourceItems.size() <= 0){
                    return;
                }

                CronologiaNumeri x = new CronologiaNumeri();
                int index = 0;
                for (int i = 0; i < sourceItems.size(); i++) {
                    CronologiaNumeri k = sourceItems.get(i);
                    if (k.getAccount().equals(acc)) {
                        x = k;
                        index = i;
                        break;
                    }
                }

                if (!numeroInserito.equals("")) { //operazione effettuata da 'account' e' 'Inserimento'
                    x.setNumeroInserito(Integer.valueOf(numeroInserito));
                } else if (!numeroEliminato.equals("")) { //operazione effettuata da 'account' e' 'eliminazione'
                    int n = Integer.valueOf(numeroEliminato);
                    if (!x.getNumeriEliminatiList().contains(n))
                        x.getNumeriEliminatiList().add(n);
                }

                sourceItems.set(index, x);

                //Se 'account' e' membro del gruppo in cui il teacher ha inserito il numero -> modifica riga del teacher
                if (Utils.isAccountInThisGruppo(g, account))
                    currentGridStudent.setItems(sourceItems);
            } else {
                //Prendi i dati da una delle grid in 'static list grid' in base al gruppo 'g'
                Grid<CronologiaNumeri> currentGrid = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGrids, "name", g.getId());
                ListDataProvider<CronologiaNumeri> sourceDataProvider = (ListDataProvider<CronologiaNumeri>) currentGrid.getDataProvider();
                List<CronologiaNumeri> sourceItems = new ArrayList<>(sourceDataProvider.getItems());

                cronologiaNumeriGridsContainerTeacher.getChildren().forEach(component -> {
                    Grid<CronologiaNumeri> grid = (Grid<CronologiaNumeri>) component;
                    if (grid.getElement().getAttribute("name") != null) {
                        if (grid.getElement().getAttribute("name").equals(g.getId())) {
                            grid.setItems(sourceItems);
                        }
                    }
                });
            }

    }

    //public methods
    public void initItemMaty(){
        ItemMaty itemMaty = matyController.getItem();
        BroadcasterSuggerisciMaty.addItems(itemMaty);
        String operazione11 = "";
        int a = 0;

        for (int i = 0; i < BroadcasterSuggerisciMaty.getItems().size(); i++) {
            try {
                operazione11 = BroadcasterSuggerisciMaty.getItems().get(i).getOperazione();
                System.out.println("Sono a a a " + operazione11);
                a = i;
            } catch (Exception e) {
            }
        }
        if (operazione11.equals("sottrazione")) {
            int numS = Integer.parseInt(BroadcasterSuggerisciMaty.getItems().get(a).getParola());
            numS = numS * 2;
            BroadcasterMaty.numeroDaSottrarre(numS + "", BroadcasterSuggerisciMaty.getItems().get(a).getParola());
            BroadcasterMaty.addIntegers(numS);
        } else {
            System.out.println("A " + a);
            BroadcasterMaty.numeroDASommare(BroadcasterMaty.getItems().get(0).getParola());
        }
        setOperazione();
    }

    public void showSelectedCronologiaNumeriGridTeacher(String currentGroup){

        cronologiaNumeriGridsContainerTeacher.getChildren().forEach(component -> {
            Grid<CronologiaNumeri> grid = (Grid<CronologiaNumeri>) component;
            if(grid.getElement().getAttribute("name") != null){
                if(grid.getElement().getAttribute("name").equals(currentGroup)){
                    grid.getStyle().set("display", "flex");
                }else{
                    grid.getStyle().set("display", "none");
                }
            }
        });

    }

    private void riempiAndShowNumeroInseritoContainerStudent(String mess, String operazione, Gruppo gruppo){
        Div box2 = Utils.getDivFromListByAttribute(containersBox, "name", gruppo.getId());
        Div wrapper2 = Utils.getDivFromListByAttribute(containersWrapper, "name", gruppo.getId());
        VerticalLayout numeroInseritoVL2 = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLList, "name", gruppo.getId());
        HorizontalLayout hor2 = new HorizontalLayout(); //contiene 'box2' (biglia) + 'eliminaBtn'

        box2.removeAll();
        wrapper2.removeAll();

        Div circle = new Div();
        circle.addClassName("circle");
        circle.setId("colorpad1");
        Paragraph paragraph = new Paragraph();
        paragraph.addClassName("parag1");
        Span span = new Span(mess);
        paragraph.add(span);
        circle.add(paragraph);
        box2.add(circle);
        hor2.add(box2);

        //se non e' il teacher -> mostra animazione pallina
        Div d = new Div();
        d.setWidth(null);
        Paragraph p = new Paragraph(mess);
        p.addClassName("parag2");
        d.addClassName("paer");
        d.setId("colorpad");
        d.add(p);

        wrapper2.add(d);
        wrapper2.addClassName("box1");
        addDivToMainContent(wrapper2, gruppo);

        hor2.add(createEliminaBtn(mess, operazione, gruppo));

        Label numeroInseritoLabel1 = new Label("Il tuo numero");
        numeroInseritoLabel1.addClassName("numeroInseritolabel");

        numeroInseritoVL2.add(numeroInseritoLabel1, hor2);
        if(Utils.isAccountInThisGruppo(gruppo, account)){
            numeroInseritoVL2.getStyle().set("display", "flex");
        }else{
            numeroInseritoVL2.getStyle().set("display", "none");
        }
    }

    private void riempiAndShowNumeroInseritoContainerTeacher(String mess, String operazione, Gruppo gruppo){
        Div box3 = Utils.getDivFromListByAttribute(containersBoxTeacher, "name", gruppo.getId());
        box3.getElement().setAttribute("id", "teacher");
        Div wrapper3 = new Div();
        VerticalLayout numeroInseritoVL3 = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLTeacherList, "name", gruppo.getId());
        HorizontalLayout hor3 = new HorizontalLayout(); //contiene 'box3' (biglia) + 'eliminaBtn'

        box3.removeAll();
        wrapper3.removeAll();

        Div circle = new Div();
        circle.addClassName("circle");
        circle.setId("colorpad1");
        Paragraph paragraph = new Paragraph();
        paragraph.addClassName("parag1");
        Span span = new Span(mess);
        paragraph.add(span);
        circle.add(paragraph);
        box3.add(circle);
        hor3.add(box3);

        hor3.add(createEliminaBtn(mess, operazione, gruppo));

        Label numeroInseritoLabel1 = new Label("Il tuo numero");
        numeroInseritoLabel1.addClassName("numeroInseritolabel");

        numeroInseritoVL3.add(numeroInseritoLabel1, hor3);

        if(MatyUI.currentGroupSelect.getId().equals(gruppo.getId())){
            numeroInseritoVL3.getStyle().set("display", "flex");
        }else{
            numeroInseritoVL3.getStyle().set("display", "none");
        }
    }

    private Button createEliminaBtn(String mess, String operazione, Gruppo gruppo){
        Button eliminaBtn = new Button("Elimina ");
        eliminaBtn.setId("eliminaBtn");
        eliminaBtn.addClassName("eliminaBtnStyle");
        eliminaBtn.addClickListener(buttonClickEvent1 -> {
            //Inizializzazione container da usare in base al gruppo
            Div box1;
            Div wrapper1;
            VerticalLayout numeroInseritoVL;
            if(!isTeacher){
                box1 = Utils.getDivFromListByAttribute(containersBox, "name", gruppo.getId());
                wrapper1 = Utils.getDivFromListByAttribute(containersWrapper, "name", gruppo.getId());
                numeroInseritoVL = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLList, "name", gruppo.getId());
            }else{
                box1 = Utils.getDivFromListByAttribute(containersBoxTeacher, "name", gruppo.getId());
                wrapper1 = new Div();
                numeroInseritoVL = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLTeacherList, "name", gruppo.getId());
            }

            numeroInseritoVL.removeAll();

            BroadcasterSuggerisciMaty.broadcast(mess, operazione, false, account, gruppo);

            box1.removeAll();
            wrapper1.removeAll();
            eliminaBtn.setEnabled(false);
            sendNumeroBtn.setEnabled(true);
            int num = Integer.parseInt(mess);
            if (operazione.equals("somma")) {
                BroadcasterMaty.addIntegers(BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1) - num);
                checkIfWin(gruppo); //include anche il teacher nella verifica
            }else{
                BroadcasterMaty.addIntegers(BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1) + num);
                checkIfWin(gruppo); //include anche il teacher nella verifica
            }
        });
        return eliminaBtn;
    }

    //Implementazione dei metodi della Java interface 'SuggerisciListenerMaty
    @Override
    public void operazione(String message, String operazione, boolean operation, Account acc, Gruppo g) {

        getUI().get().access(() -> {

            if(operation == true){ //se 'true' -> inserimento numero
                updateAllGrid(message, "", acc, g);
            }else{ //se false -> rimozione di un numero
                updateAllGrid("", message, acc, g);
            }

        });
    }

    @Override
    public void refreshContent(){
        UI.getCurrent().access(() -> {
            if (isTeacher) {
                refreshContentForTeacher();
            } else {
                Gruppo currentGruppo = Utils.findGruppoByAccount(gruppi, account);
                updateAllGrid("", "", account, currentGruppo);
            }
        });
    }

    public void refreshContentForTeacher(){
        UI.getCurrent().access(() -> {
            for (Gruppo g : gruppi) {
                updateAllGrid("", "", account, g);
            }
        });
    }


    @Override
    public void reset(){
        containersBox.clear();
        containersBoxTeacher.clear();
        containersWrapper.clear();
        numeroInseritoVLList.clear();
        numeroInseritoVLTeacherList.clear();
        cronologiaGridsListTeacher.clear();
        cronologiaGrids.clear();
        isCronologiaGridsSetted = false;
    }

    //Getter and setter
    public ArrayList<Div> getContainersBoxTeacher() {
        return containersBoxTeacher;
    }

    public ArrayList<VerticalLayout> getNumeroInseritoVLTeacherList() {
        return numeroInseritoVLTeacherList;
    }

    public HorizontalLayout getCronologiaNumeriGridsContainerTeacher() {
        return cronologiaNumeriGridsContainerTeacher;
    }
}
