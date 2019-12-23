package com.example.demo.maty.gameMenagement.frondend;


import com.example.demo.entity.Account;
import com.example.demo.entity.CronologiaNumeri;
import com.example.demo.entity.Gruppo;
import com.example.demo.error.ErrorPage;
import com.example.demo.maty.gameMenagement.backend.MatyController;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterMaty;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterSuggerisciMaty;
import com.example.demo.maty.gameMenagement.backend.db.ItemMaty;
import com.example.demo.maty.gameMenagement.backend.listeners.SuggerisciListenerMaty;
import com.example.demo.utility.Utils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
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

    //static field
    private static List<Grid<CronologiaNumeri>> cronologiaGrids = new ArrayList<Grid<CronologiaNumeri>>();
    private static List<Grid<CronologiaNumeri>> cronologiaGridsTeacher = new ArrayList<Grid<CronologiaNumeri>>();
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
    //Nuove variabili
    private VerticalLayout interactionContainer;
    private VerticalLayout cronologiaGridContainer;
    private HorizontalLayout cronologiaNumeriGridsContainer;
    private HorizontalLayout cronologiaNumeriGridsContainerTeacher;
    private Div numeroInserito = new Div();
    private Grid<CronologiaNumeri> currentGrid; //per lo studente per risolvere 'Cannot access state in VaadinSession or UI without locking the session.'


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
            currentGrid = new Grid<>(CronologiaNumeri.class);

            BroadcasterSuggerisciMaty.register(account, this);

            //interactionContainer: contiene 'operazioneLabel', textfield, 'sommaBtn/sottrazioneBtn', numero inserito
            interactionContainer = new VerticalLayout();
            interactionContainer.setPadding(false);
            interactionContainer.addClassName("interactionContainerStyle");
            riempiInteractionContainer();
            add(interactionContainer);

            //'cronologiaNumeriGridsContainer'
            cronologiaGridContainer = new VerticalLayout();
            cronologiaGridContainer.setPadding(false);
            cronologiaGridContainer.setWidth("100%");
            cronologiaGridContainer.setHeight("100%");

            Label cronologiaMosse = new Label("Cosa hanno fatto fino ad ora");
            cronologiaMosse.addClassName("cronologiaMosse");

            cronologiaNumeriGridsContainer = new HorizontalLayout();
            cronologiaNumeriGridsContainer.setWidth("500px");
            cronologiaNumeriGridsContainer.setHeight("300px");

            cronologiaNumeriGridsContainerTeacher = new HorizontalLayout();
            cronologiaNumeriGridsContainerTeacher.setWidth("500px");
            cronologiaNumeriGridsContainerTeacher.setHeight("300px");

            if(!isCronologiaGridsSetted) {
                createGridForCronologiaNumeri();
                createGridForCronologiaNumeriForTeacher();
                isCronologiaGridsSetted = true;
            }

            initGridCronologiaNumeriForAll();

            initArrayListsAndAddToMainContent(); //ArrayList usati per implementare la gestione dei gruppi, un container per ogni gruppo

            if(!isTeacher) {
                cronologiaGridContainer.add(cronologiaMosse, cronologiaNumeriGridsContainer);
            }else{
                cronologiaGridContainer.add(cronologiaMosse, cronologiaNumeriGridsContainerTeacher);
            }
            add(cronologiaGridContainer);

            showGrids();

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

        Div divC = new Div();  //container per il btn (vedi 'buttonsend1.scss)
        divC.addClassName("container11");
        sendNumeroBtn.setId("btn");
        sendNumeroBtn.addClassName("button12");
        sendNumeroBtn.getStyle().set("cursor", "pointer");
        divC.add(sendNumeroBtn);
        sendNumeroInserito(); //business logic of sendNumeroBtn click listener
        setOperazione();

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

    @Override
    public void operazione(String message, String operazione, boolean operation, Gruppo g) {
        getUI().get().access(() -> {

            Grid<CronologiaNumeri> currentGrid = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGrids, "name", g.getId());
            CronologiaNumeri x = new CronologiaNumeri();

            ListDataProvider<CronologiaNumeri> sourceDataProvider = (ListDataProvider<CronologiaNumeri>) currentGrid.getDataProvider();
            List<CronologiaNumeri> sourceItems = new ArrayList<>(sourceDataProvider.getItems());
            int index = 0;
            for(int i = 0; i < sourceItems.size(); i++){
                CronologiaNumeri k = sourceItems.get(i);
                if(k.getAccount().equals(account)){
                    x = k;
                    index = i;
                    break;
                }
            }

            x.setGruppo(Utils.findGruppoByAccount(gruppi, account));
            if(operation == true){ //se 'true' -> inserimento numero
                x.setNumeroAttuale(numeroInserito);
            }else{ //se false -> rimozione di un numero
                x.getNumeriEliminatiList().add(Integer.valueOf(message));
            }

            //aggiorna valore sia nella list che nella grid
            sourceItems.set(index, x);
            currentGrid.setItems(sourceItems);
        });
    }

    private void setOperazione(){
        for (int i = 0; i < BroadcasterSuggerisciMaty.getItems().size(); i++) {
            try {
                System.out.println("StartGameMatyUI: items:" + BroadcasterSuggerisciMaty.getItems().get(i).getOperazione());
                String operazione = BroadcasterSuggerisciMaty.getItems().get(i).getOperazione();
                if (operazione.equalsIgnoreCase("somma")) {
                    operazioneLabel.setText("Somma un numero");
                    sendNumeroBtn.setText("Somma");
                } else {
                    operazioneLabel.setText("Sottrai un numero");
                    sendNumeroBtn.setText("Sottrai");
                }
            } catch (Exception e) {
                //e.printStackTrace();  -> NullPointerException con ItemMaty
            }
        }
    }

    private void checkIfWin() {
        for (int i = 0; i < BroadcasterMaty.getPartiteThread().size(); i++) {
            if (i == 0) {
                flag = false;
            }
            try {
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
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
                if (vincente == false && flag == false) {
                } else if (vincente == true && flag == false) {
                    BroadcasterMaty.getPartiteThread().get(i).interrupt();
                    BroadcasterMaty.getPartiteThread().get(i).stopTimer();
                    BroadcasterMaty.partitaVincente("" + BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1)
                            ,punteggio);
                }
            }

        }
    }

    //Send button: funzionamento di tutto il gioco quando viene premuto il pulsante 'Somma / Sottrai'
    private void sendNumeroInserito(){
        sendNumeroBtn.addClickListener(buttonClickEvent -> {
            Gruppo gruppo; //gruppo da cui parte l'azione
            VerticalLayout numeroInseritoVL;
            if(isTeacher){
                gruppo = Utils.findGruppoByName(gruppi, MatyUI.currentGroupSelect.getId());
                numeroInseritoVL = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLTeacherList, "name", gruppo.getId());
            }else{
                gruppo = Utils.findGruppoByAccount(gruppi, account);
                numeroInseritoVL = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLList, "name", gruppo.getId());
            }

            numeroInseritoVL.removeAll();
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
                                    operazioneSomma(i, mess, operazione, gruppo);
                                } else {
                                    operazioneSottrazione(i, mess, operazione, gruppo);
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

    private void operazioneSomma(int i, String mess, String operazione, Gruppo gruppo){ //sendBtn = 'Somma'
        if (BroadcasterMaty.getIntegers().size() == 0) {
            BroadcasterMaty.addIntegers(Integer.parseInt(mess));
        } else {
            BroadcasterMaty.addIntegers(
                    BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1) + Integer.parseInt(mess));
        }

        BroadcasterSuggerisciMaty.broadcast(mess, operazione, true, gruppo);

        /*----INIZIO------------------------------------------------------------------------------------------------------------------------------------*/
        int j;
        if (operazione.equals("somma")) {
            j = 0;
        } else {
            j = 1;
        }

        //Inizializzazione container da usare in base al gruppo
        Div box2;
        Div wrapper2;
        VerticalLayout numeroInseritoVL2;
        HorizontalLayout hor2 = new HorizontalLayout(); //contiene 'box2' (biglia) + 'eliminaBtn'
        if(!isTeacher){
            box2 = Utils.getDivFromListByAttribute(containersBox, "name", gruppo.getId());
            wrapper2 = Utils.getDivFromListByAttribute(containersWrapper, "name", gruppo.getId());
            numeroInseritoVL2 = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLList, "name", gruppo.getId());
        }else{
            box2 = Utils.getDivFromListByAttribute(containersBoxTeacher, "name", gruppo.getId());
            box2.getElement().setAttribute("id", "teacher");
            wrapper2 = new Div();
            numeroInseritoVL2 = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLTeacherList, "name", gruppo.getId());
        }

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
        numeroInserito = box2;
        hor2.add(box2);

        if(!isTeacher) { //se non e' il teacher -> mostra animazione pallina
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

            getElement().executeJs("setRandomColor()");
        }

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

            BroadcasterSuggerisciMaty.broadcast(mess, operazione, false, gruppo);

            box1.removeAll();
            wrapper1.removeAll();
            eliminaBtn.setEnabled(false);
            sendNumeroBtn.setEnabled(true);
            int num = Integer.parseInt(mess);
            if (operazione.equals("somma")) {
                BroadcasterMaty.addIntegers(BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1) - num);
                checkIfWin();
            }

        });
        hor2.add(eliminaBtn);

        Label numeroInseritoLabel1 = new Label("Il tuo numero");
        numeroInseritoLabel1.addClassName("numeroInseritolabel");

        numeroInseritoVL2.add(numeroInseritoLabel1, hor2);

        if(Utils.isAccountInThisGruppo(gruppo, account)){
            numeroInseritoVL2.getStyle().set("display", "flex");
        }else{
            numeroInseritoVL2.getStyle().set("display", "none");
        }

        /*--FINE--------------------------------------------------------------------------------------------------------------------------------------*/
        suggerisciNumero.setValue(suggerisciNumero.getEmptyValue());
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
        checkIfWin();
    }

    private void operazioneSottrazione(int i, String mess, String operazione, Gruppo gruppo){ //sendBtn = 'Sottrai'
        System.out.println(operazione);
        System.out.println(BroadcasterSuggerisciMaty.getItems().get(i).getParola());
        BroadcasterMaty.addIntegers(
                BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1) - Integer.parseInt(mess));

        BroadcasterSuggerisciMaty.broadcast(mess, operazione, true, gruppo);

        /*------INIZIO----------------------------------------------------------------------------------------------------------------------------------*/
        int j;
        if (operazione.equals("somma")) {
            j = 0;
        } else {
            j = 1;
        }

        //Inizializzazione container da usare in base al gruppo
        Div box3;
        Div wrapper3;
        VerticalLayout numeroInseritoVL3;
        HorizontalLayout hor3 = new HorizontalLayout(); //contiene 'box1' (biglia) + 'eliminaBtn'
        if(!isTeacher){
            box3 = Utils.getDivFromListByAttribute(containersBox, "name", gruppo.getId());
            wrapper3 = Utils.getDivFromListByAttribute(containersWrapper, "name", gruppo.getId());
            numeroInseritoVL3 = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLList, "name", gruppo.getId());
        }else{
            box3 = Utils.getDivFromListByAttribute(containersBoxTeacher, "name", gruppo.getId());
            wrapper3 = new Div();
            numeroInseritoVL3 = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLTeacherList, "name", gruppo.getId());
        }

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
        numeroInserito = box3;
        hor3.add(box3);

        if(!isTeacher) { //se non e' il teacher -> mostra animazione pallina
            Div d = new Div();
            d.setWidth(null);
            Paragraph p = new Paragraph(mess);
            p.addClassName("parag2");
            d.addClassName("paer");
            d.setId("colorpad");
            d.add(p);
            wrapper3.add(d);
            wrapper3.addClassName("box1");
            addDivToMainContent(wrapper3, gruppo);
            getElement().executeJs("setRandomColor()");
        }

        Button eliminaBtn = new Button("Elimina");
        eliminaBtn.setId("eliminaBtn");
        eliminaBtn.addClassName("eliminaBtnStyle");
        eliminaBtn.addClickListener(buttonClickEvent1 -> {
            //Inizializzazione container da usare in base al gruppo
            Div box4;
            Div wrapper4;
            VerticalLayout numeroInseritoVL;
            if(!isTeacher){
                box4 = Utils.getDivFromListByAttribute(containersBox, "name", gruppo.getId());
                wrapper4 = Utils.getDivFromListByAttribute(containersWrapper, "name", gruppo.getId());
                numeroInseritoVL = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLList, "name", gruppo.getId());
            }else{
                box4 = Utils.getDivFromListByAttribute(containersBoxTeacher, "name", gruppo.getId());
                wrapper4 = new Div();
                numeroInseritoVL = Utils.getVerticalLayoutFromListByAttribute(numeroInseritoVLTeacherList, "name", gruppo.getId());
            }

            numeroInseritoVL.removeAll();

            BroadcasterSuggerisciMaty.broadcast(mess, operazione, false, gruppo);

            box4.removeAll();
            wrapper4.removeAll();
            eliminaBtn.setEnabled(false);
            sendNumeroBtn.setEnabled(true);
            int num = Integer.parseInt(mess);
            BroadcasterMaty.addIntegers(BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1) + num);
            checkIfWin();
        });
        hor3.add(eliminaBtn);

        Label numeroInseritoLabel1 = new Label("Il tuo numero");
        numeroInseritoLabel1.addClassName("numeroInseritolabel");

        numeroInseritoVL3.add(numeroInseritoLabel1, hor3);

        if(Utils.isAccountInThisGruppo(gruppo, account)){
            numeroInseritoVL3.getStyle().set("display", "flex");
        }else{
            numeroInseritoVL3.getStyle().set("display", "none");
        }

        /*------FINE----------------------------------------------------------------------------------------------------------------------------------*/


        suggerisciNumero.setValue(suggerisciNumero.getEmptyValue());
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
        checkIfWin();
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

        for(Grid<CronologiaNumeri> i : cronologiaGridsTeacher){
            i.getStyle().set("display", "none");
        }

    }

    private void createGridForCronologiaNumeri(){
        for(int i = 0; i < gruppi.size(); i++){
            Gruppo g = gruppi.get(i);
            Grid<CronologiaNumeri> grid = new Grid<>(CronologiaNumeri.class);
            grid.getElement().setAttribute("name", g.getId());
            grid.removeAllColumns();
            grid.addColumn(CronologiaNumeri::getNomeAccount).setHeader(" ");
            grid.addColumn(TemplateRenderer.<CronologiaNumeri>of("[[item.name]]")
                    .withProperty("name", CronologiaNumeri::getNumeroAttualeWithHTML)).setHeader("Numero attuale");
            grid.addColumn(CronologiaNumeri::getNumeriEliminatiListWithString).setHeader("Numeri eliminati");
            grid.setWidth("100%");
            grid.setHeight("100%");
            grid.getStyle().set("display", "none");

            cronologiaGrids.add(grid);
        }
    }

    private void createGridForCronologiaNumeriForTeacher(){
        for(int i = 0; i < gruppi.size(); i++){
            Gruppo g = gruppi.get(i);
            Grid<CronologiaNumeri> grid = new Grid<>(CronologiaNumeri.class);
            grid.getElement().setAttribute("id", "teacher");
            grid.getElement().setAttribute("name", g.getId());
            grid.removeAllColumns();
            grid.addColumn(CronologiaNumeri::getNomeAccount).setHeader(" ");
            grid.addColumn(TemplateRenderer.<CronologiaNumeri>of("[[item.name]]")
                    .withProperty("name", CronologiaNumeri::getNumeroAttualeWithHTML)).setHeader("Numero attuale");
            //grid.addColumn(CronologiaNumeri::getNumeroAttuale).setHeader("Numero attuale");

            grid.addColumn(CronologiaNumeri::getNumeriEliminatiListWithString).setHeader("Numeri eliminati");
            grid.setWidth("100%");
            grid.setHeight("100%");
            grid.getStyle().set("display", "none");

            cronologiaGridsTeacher.add(grid);
        }
    }

    private void initGridCronologiaNumeriForAll(){

        if(!isTeacher) {
            Gruppo currentGruppo = Utils.findGruppoByAccount(gruppi, account);
            Grid<CronologiaNumeri> currentGrid = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGrids, "name", currentGruppo.getId());
            Grid<CronologiaNumeri> currentGridTeacher = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGridsTeacher, "name", currentGruppo.getId());

            ListDataProvider<CronologiaNumeri> sourceDataProvider = (ListDataProvider<CronologiaNumeri>) currentGrid.getDataProvider();
            List<CronologiaNumeri> sourceItems = new ArrayList<>(sourceDataProvider.getItems());

            ListDataProvider<CronologiaNumeri> sourceDataProviderTeacher = (ListDataProvider<CronologiaNumeri>) currentGridTeacher.getDataProvider();
            List<CronologiaNumeri> sourceItemsTeacher = new ArrayList<>(sourceDataProviderTeacher.getItems());

            for (Account x : currentGruppo.getMembri()) {
                CronologiaNumeri y = new CronologiaNumeri(account);
                if (!sourceItems.contains(y)) {
                    y.setGruppo(currentGruppo);
                    sourceItems.add(y);
                    sourceItemsTeacher.add(y);
                }
            }
            currentGrid.setItems(sourceItems);
            currentGridTeacher.setItems(sourceItemsTeacher);

        }else{
            for(Gruppo x : gruppi){  //Aggiungi account del teacher in tutte le grid
                Grid<CronologiaNumeri> currentGrid = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGrids, "name", x.getId());
                ListDataProvider<CronologiaNumeri> sourceDataProvider = (ListDataProvider<CronologiaNumeri>) currentGrid.getDataProvider();
                List<CronologiaNumeri> sourceItems = new ArrayList<>(sourceDataProvider.getItems());

                Grid<CronologiaNumeri> currentGridTeacher = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGridsTeacher, "name", x.getId());
                ListDataProvider<CronologiaNumeri> sourceDataProviderTeacher = (ListDataProvider<CronologiaNumeri>) currentGridTeacher.getDataProvider();
                List<CronologiaNumeri> sourceItemsTeacher = new ArrayList<>(sourceDataProviderTeacher.getItems());

                CronologiaNumeri y = new CronologiaNumeri(account); //teacher account
                y.setGruppo(x);
                sourceItems.add(y);
                sourceItemsTeacher.add(y);

                currentGrid.setItems(sourceItems);
                currentGridTeacher.setItems(sourceItemsTeacher);
                cronologiaNumeriGridsContainerTeacher.add(currentGridTeacher);
            }
        }
    }

    private void showGrids(){

        if(isTeacher) {
            Gruppo currentGruppo = Utils.findGruppoByName(gruppi, MatyUI.currentGroupSelect.getId());
            cronologiaNumeriGridsContainerTeacher.getChildren().forEach(component -> {
                Grid<CronologiaNumeri> grid = (Grid<CronologiaNumeri>) component;
                if(grid.getElement().getAttribute("name") != null){
                    if(grid.getElement().getAttribute("name").equals(currentGruppo.getId())){
                        grid.getStyle().set("display", "flex");
                    }else{
                        grid.getStyle().set("display", "none");
                    }
                }
            });

        }else{

            //Clonazione di un oggetto che viene prelevato da 'cronologiaGrids' (risolve 'Cannot access state in VaadinSession or UI without locking the session.'=
            //NOTA: Ogni update effettuato su 'currentGrid', dovra' essere inserito manualmente anche in 'cronologiaGrids'
            Gruppo currentGruppo = Utils.findGruppoByAccount(gruppi, account);
            Grid<CronologiaNumeri> grid = Utils.getGridCronologiaNumeriFromListByAttribute(cronologiaGrids, "name", currentGruppo.getId());

            currentGrid.getElement().setAttribute("name", grid.getElement().getAttribute("name"));
            currentGrid.removeAllColumns();
            currentGrid.addColumn(CronologiaNumeri::getNomeAccount).setHeader(" ");
            currentGrid.addColumn(TemplateRenderer.<CronologiaNumeri>of("[[item.name]]")
                    .withProperty("name", CronologiaNumeri::getNumeroAttualeWithHTML)).setHeader("Numero attuale");
            currentGrid.addColumn(CronologiaNumeri::getNumeriEliminatiListWithString).setHeader("Numeri eliminati");
            currentGrid.setWidth("100%");
            currentGrid.setHeight("100%");

            ListDataProvider<CronologiaNumeri> sourceDataProvider = (ListDataProvider<CronologiaNumeri>) grid.getDataProvider();
            List<CronologiaNumeri> sourceItems = new ArrayList<>(sourceDataProvider.getItems());

            currentGrid.setItems(sourceItems);
            cronologiaNumeriGridsContainer.add(currentGrid);
        }
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
