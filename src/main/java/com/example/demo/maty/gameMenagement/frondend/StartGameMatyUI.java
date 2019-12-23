package com.example.demo.maty.gameMenagement.frondend;


import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.error.ErrorPage;
import com.example.demo.maty.gameMenagement.backend.MatyController;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterMaty;
import com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterSuggerisciMaty;
import com.example.demo.maty.gameMenagement.backend.db.ItemMaty;
import com.example.demo.maty.gameMenagement.backend.listeners.SuggerisciListenerMaty;
import com.example.demo.userOperation.NavBar;
import com.example.demo.utility.MessageList;
import com.example.demo.utility.Utils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;

@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/style.css")
@StyleSheet("frontend://stile/button.css")
@StyleSheet("frontend://stile/buttonsend1.scss")
@StyleSheet("frontend://stile/divbox.css")
@StyleSheet("frontend://stile/animation.css")
@JavaScript("frontend://js/script.js")
public class StartGameMatyUI extends HorizontalLayout implements SuggerisciListenerMaty {

    //private VerticalLayout parolaLayout;
    //private VerticalLayout cronologiaNUmeri;
    private Button button;
    private MatyController matyController;
    private boolean vincente;
    private boolean flag = false;
    private Label operazioneLabel = new Label();
    private Label paroleVotateLabel;
    private Button sendParola = new Button();
    private Account account;
    private Div containerNumeriSS = new Div(); //-> RIMUOVERE PER NON UTILIZZO
    //private Div box = new Div();    //container per il numero inserito (biglia con numero all'interno e si trova sotto "Il tuo numero e':")
    //private Div wrapper = new Div();    //<div class='box1'> contiene html dedicato per la pallina che si muove sullo schermo
    private Label cronologiaMosse;
    private TextField suggerisci;
    private boolean isTeacher;
    private VerticalLayout paroleLayoutContainer;
    private VerticalLayout cronologiaNumeriContainer;
    private List<Gruppo> gruppi = new ArrayList<Gruppo>();
    private ArrayList<Div> containersBox;
    private ArrayList<Div> containersBoxTeacher;
    private ArrayList<Div> containersWrapper;
    private ArrayList<VerticalLayout> parolaLayoutList;
    private ArrayList<VerticalLayout> parolaLayoutTeacherList;
    private ArrayList<VerticalLayout> cronologiaNUmeriList;
    private ArrayList<VerticalLayout> cronologiaNUmeriTeacherList;

    public StartGameMatyUI(MatyController matyController, Account account, boolean isTeacher) {

        try {
            //Inizializzazione
            setId("StartGameMatyUI");
            this.account = account;
            this.matyController = matyController;
            this.isTeacher = isTeacher;
            gruppi = com.example.demo.users.broadcaster.Broadcaster.getGruppiListReceive();
            containersBox = new ArrayList<Div>();
            containersBoxTeacher = new ArrayList<Div>();
            containersWrapper = new ArrayList<Div>();
            parolaLayoutList = new ArrayList<VerticalLayout>();
            parolaLayoutTeacherList = new ArrayList<VerticalLayout>();
            cronologiaNUmeriList = new ArrayList<VerticalLayout>();
            cronologiaNUmeriTeacherList = new ArrayList<VerticalLayout>();

            BroadcasterSuggerisciMaty.register(account, this);

            if(isTeacher){
                getStyle().set("height", "280px"); //necessario per visualizzare la label 'Tempo' nella posizione corretta
            }

            containerNumeriSS.addClassName("containerNumeriSS");
            //box.addClassName("box");

            Image image = new Image("frontend/img/Maty.jpeg", "maty");
            image.setWidth("200px");
            image.setHeight("200px");

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.addClassName("horizontalLayoutStartGameUI");
            suggerisci = new TextField();
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
            operazioneLabel.addClassName("labelSuggerisci");

            if(isTeacher){  //se teacher, rimuovi logo di maty
                add(operazioneLabel);
            }else {
                getStyle().set("margin-top", NavBar.NAVBAR_HORIZONTAL_HEIGHT);
                add(operazioneLabel, image);
            }

            Div divC = new Div();
            divC.addClassName("container11");
            sendParola.setId("btn");
            sendParola.addClassName("button12");
            divC.add(sendParola);
            sendNumeroInserito(); //business logic of sendParola click listener

            paroleVotateLabel = new Label();
            paroleVotateLabel.addClassName("parolevotatelabel");
            add(paroleVotateLabel);
            sendParola.getStyle().set("cursor", "pointer");
            sendParola.setWidth(null);
            //parolaLayout.addClassName("suggerisciParolaLayout");

            //parolaLayout.setWidth("250px");

            horizontalLayout.add(suggerisci, divC);

            setOperazione();


            paroleLayoutContainer = new VerticalLayout();
            paroleLayoutContainer.setSpacing(false);
            paroleLayoutContainer.setPadding(false);

            VerticalLayout cronologiaContainer = new VerticalLayout();
            cronologiaContainer.setSpacing(false);
            cronologiaContainer.setPadding(false);
            cronologiaMosse = new Label("Cosa hanno fatto fino ad ora:");
            cronologiaMosse.addClassName("cronologiaMosse");

            cronologiaNumeriContainer = new VerticalLayout();
            cronologiaNumeriContainer.setPadding(false);
            cronologiaNumeriContainer.setSpacing(false);

            cronologiaContainer.add(cronologiaMosse, cronologiaNumeriContainer);

            horizontalLayout.add(paroleLayoutContainer, cronologiaContainer);

            initArrayListsAndAddToMainContent(); //ArrayList usati per implementare la gestione dei gruppi, un container per ogni gruppo

            add(horizontalLayout);
        } catch (Exception e) {
            removeAll();
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
            e.printStackTrace();
        }

    }

    private void initArrayListsAndAddToMainContent(){
        for(Gruppo x : gruppi){
            //Div class='box'
            Div d = new Div();
            d.addClassName("box");
            d.getElement().setAttribute("name", x.getId());
            d.getStyle().set("display", "none");
            containersBox.add(d);

            Div d3 = new Div();
            d3.addClassName("box");
            d3.getElement().setAttribute("name", x.getId());
            d3.getStyle().set("display", "none");
            containersBoxTeacher.add(d3);

            //Div 'wrapper' ->  usato per mostrare la biglia in movimento (SOLO student)
            Div d2 = new Div();  //e' un container che contiene a sua volta il 'wrapper'
            d2.getElement().setAttribute("name", x.getId());
            d2.getStyle().set("display", "none");
            containersWrapper.add(d2);

            //VerticalLayout ParolaLayout
            VerticalLayout vert = new VerticalLayout();
            vert.getElement().setAttribute("name", x.getId());
            vert.addClassName("suggerisciParolaLayout");
            vert.setWidth("250px");
            vert.getStyle().set("display", "none");
            parolaLayoutList.add(vert);

            VerticalLayout vert2 = new VerticalLayout();
            vert2.getElement().setAttribute("id", "teacher");
            vert2.getElement().setAttribute("name", x.getId());
            vert2.addClassName("suggerisciParolaLayoutTeacher");
            vert2.getStyle().set("display", "none");
            parolaLayoutTeacherList.add(vert2);

            //VerticalLayout 'cronologiaNumeri
            VerticalLayout vert3 = new VerticalLayout();
            vert3.setPadding(false);
            vert3.setSpacing(false);
            vert3.addClassName("cronologiaNumeri");
            vert3.getElement().setAttribute("name", x.getId());
            vert3.getStyle().set("display", "none");
            cronologiaNUmeriList.add(vert3);
            cronologiaNumeriContainer.add(vert3);

            VerticalLayout vert4 = new VerticalLayout();
            vert4.setPadding(false);
            vert4.setSpacing(false);
            vert4.addClassName("cronologiaNumeriTeacher");
            vert4.getElement().setAttribute("id", "teacher");
            vert4.getElement().setAttribute("name", x.getId());
            vert4.getStyle().set("display", "none");
            cronologiaNUmeriTeacherList.add(vert4);
            cronologiaNumeriContainer.add(vert4);
        }
    }

    @Override
    public void operazione(String message, String operazione, String nome, boolean operation, Gruppo g) {
        getUI().get().access(() -> {
            MessageList messageList = new MessageList("message-list");
            Div div = new Div();
            Button label = null;

            if (operation == true) {
                label = new Button(nome + " ha inserito " + message);
            }else {
                label = new Button(nome + " ha eliminato " + message);
            }
            //div.addClassName("labelCronologia");
            //label.getStyle().set("margin-right", "15px");
            label.setEnabled(false);
            label.setId("button");
            div.add(label);
            div.getStyle().set("margin-bottom", "12px");
            messageList.add(div);

            VerticalLayout cronologiaNUmeri;
            if(isTeacher){
                cronologiaNUmeri = Utils.getVerticalLayoutFromListByAttribute(cronologiaNUmeriTeacherList, "name", g.getId());
            }else{
                cronologiaNUmeri = Utils.getVerticalLayoutFromListByAttribute(cronologiaNUmeriList, "name", g.getId());
            }

            if (!account.getNome().equals(nome)) {
                cronologiaNUmeri.add(messageList);
                if(Utils.isAccountInThisGruppo(g, account)){
                    cronologiaNUmeri.getStyle().set("display", "flex");
                }else{
                    cronologiaNUmeri.getStyle().set("display", "none");
                }
            }

        });
    }

    public void setOperazione(){
        for (int i = 0; i < BroadcasterSuggerisciMaty.getItems().size(); i++) {
            try {
                System.out.println("StartGameMatyUI: items:" + BroadcasterSuggerisciMaty.getItems().get(i).getOperazione());
                String operazione = BroadcasterSuggerisciMaty.getItems().get(i).getOperazione();
                if (operazione.equalsIgnoreCase("somma")) {
                    operazioneLabel.setText("Somma un numero");
                    paroleVotateLabel.setText("Il tuo numero è: ");
                    sendParola.setText("Somma");
                } else {
                    operazioneLabel.setText("Sottrai un numero");
                    paroleVotateLabel.setText("Il tuo numero è: ");
                    sendParola.setText("Sottrai");
                }
            } catch (Exception e) {
                //e.printStackTrace();  -> NullPointerException con ItemMaty
            }
        }
    }

    void checkIfWin() {
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

    private void deleteButtonStyle() {
        button.getStyle().set("margin-left", "30px");
        button.getStyle().set("cursor", "pointer");
        button.getStyle().set("color", "white");
    }

    //Send button: funzionamento di tutto il gioco quando viene premuto il pulsante 'Somma / Sottrai'
    private void sendNumeroInserito(){
        sendParola.addClickListener(buttonClickEvent -> {
            Gruppo gruppo; //gruppo da cui parte l'azione
            VerticalLayout parolaLayout;
            if(isTeacher){
                gruppo = Utils.findGruppoByName(gruppi, MatyUI.currentGroupSelect.getId());
                parolaLayout = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutTeacherList, "name", gruppo.getId());
            }else{
                gruppo = Utils.findGruppoByAccount(gruppi, account);
                parolaLayout = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutList, "name", gruppo.getId());
            }

            parolaLayout.removeAll();
            getElement().executeJs("sends()");
            String mess = suggerisci.getValue();
            if (!mess.equals(BroadcasterMaty.getItems().get(0).getParola())) {
                if (!mess.equals("")) {
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
                        suggerisci.setValue("");
                    }
                }
            }else {
                suggerisci.setValue("");
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
        if(isTeacher) {
            BroadcasterSuggerisciMaty.broadcast(mess, operazione, "Teacher", true, gruppo);
        }else{
            BroadcasterSuggerisciMaty.broadcast(mess, operazione, account.getNome(), true, gruppo);
        }

        /*----INIZIO------------------------------------------------------------------------------------------------------------------------------------*/
        int j;
        containerNumeriSS.removeAll();
        if (operazione.equals("somma")) {
            j = 0;
        } else {
            j = 1;
        }

        //Inizializzazione container da usare in base al gruppo
        Div box2;
        Div wrapper2;
        if(!isTeacher){
            box2 = Utils.getDivFromListByAttribute(containersBox, "name", gruppo.getId());
            wrapper2 = Utils.getDivFromListByAttribute(containersWrapper, "name", gruppo.getId());
        }else{
            box2 = Utils.getDivFromListByAttribute(containersBoxTeacher, "name", gruppo.getId());
            box2.getElement().setAttribute("id", "teacher");
            wrapper2 = new Div();
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
        addDivToMainContent(box2, gruppo);
        //add(box2);

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
            //add(wrapper2);

            getElement().executeJs("setRandomColor()");
        }
        MessageList messageList = new MessageList("message-list");

        Div div = new Div();
        Label label = new Label(mess);
        label.getStyle().set("margin-right", "15px");
        button = new Button("Elimina ", VaadinIcon.MINUS.create());
        button.setId("button");
        button.addClickListener(buttonClickEvent1 -> {
            //Inizializzazione container da usare in base al gruppo
            Div box1;
            Div wrapper1;
            VerticalLayout parolaLayout;
            if(!isTeacher){
                box1 = Utils.getDivFromListByAttribute(containersBox, "name", gruppo.getId());
                wrapper1 = Utils.getDivFromListByAttribute(containersWrapper, "name", gruppo.getId());
                parolaLayout = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutList, "name", gruppo.getId());
            }else{
                box1 = Utils.getDivFromListByAttribute(containersBoxTeacher, "name", gruppo.getId());
                wrapper1 = new Div();
                parolaLayout = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutTeacherList, "name", gruppo.getId());
            }

            parolaLayout.removeAll();
            if(isTeacher) {
                BroadcasterSuggerisciMaty.broadcast(mess, operazione, "Teacher", false, gruppo);
            }else{
                BroadcasterSuggerisciMaty.broadcast(mess, operazione, account.getNome(), false, gruppo);
            }

            box1.removeAll();
            wrapper1.removeAll();
            button.setEnabled(false);
            sendParola.setEnabled(true);
            int num = Integer.parseInt(mess);
            if (operazione.equals("somma")) {
                BroadcasterMaty.addIntegers(
                        BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1) - num);
                box1.removeAll();
                wrapper1.removeAll();
                Div circle1 = new Div();
                circle1.addClassName("circle");
                circle1.setId("colorpad1");
                Paragraph paragraph1 = new Paragraph();
                paragraph1.addClassName("parag1");
                Span span1 = new Span("0");
                paragraph1.add(span1);
                circle1.add(paragraph1);
                box1.add(circle1);
                addDivToMainContent(box1, gruppo);
                //add(box1);

                if(!isTeacher) { //se non e' il teacher -> mostra animazione pallina
                    Div d1 = new Div();
                    d1.setWidth(null);
                    Paragraph p1 = new Paragraph("0");
                    p1.addClassName("parag2");
                    d1.addClassName("paer");
                    d1.setId("colorpad");
                    d1.add(p1);
                    wrapper1.add(d1);
                    wrapper1.addClassName("box1");
                    addDivToMainContent(wrapper1, gruppo);
                    //add(wrapper1);
                    getElement().executeJs("setRandomColor()");
                }
                checkIfWin();
            }

        });
        //deleteButtonStyle();
        div.add(label, button);
        messageList.add(div);

        VerticalLayout parolaLayout;
        if(isTeacher){
            parolaLayout = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutTeacherList, "name", gruppo.getId());
        }else{
            parolaLayout = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutList, "name", gruppo.getId());
        }
        parolaLayout.add(messageList);
        if(Utils.isAccountInThisGruppo(gruppo, account)){
            parolaLayout.getStyle().set("display", "flex");
        }else{
            parolaLayout.getStyle().set("display", "none");
        }

        /*--FINE--------------------------------------------------------------------------------------------------------------------------------------*/
        suggerisci.setValue("");
        sendParola.setEnabled(false);
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
        if(isTeacher) {
            BroadcasterSuggerisciMaty.broadcast(mess, operazione, "Teacher", true, gruppo);
        }else{
            BroadcasterSuggerisciMaty.broadcast(mess, operazione, account.getNome(), true, gruppo);
        }
        /*------INIZIO----------------------------------------------------------------------------------------------------------------------------------*/
        int j;
        containerNumeriSS.removeAll();
        if (operazione.equals("somma")) {
            j = 0;
        } else {
            j = 1;
        }

        //Inizializzazione container da usare in base al gruppo
        Div box3;
        Div wrapper3;
        if(!isTeacher){
            box3 = Utils.getDivFromListByAttribute(containersBox, "name", gruppo.getId());
            wrapper3 = Utils.getDivFromListByAttribute(containersWrapper, "name", gruppo.getId());
        }else{
            box3 = Utils.getDivFromListByAttribute(containersBoxTeacher, "name", gruppo.getId());
            wrapper3 = new Div();
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
        addDivToMainContent(box3, gruppo);
        //add(box3);

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
            //add(wrapper3);
            getElement().executeJs("setRandomColor()");
        }

        MessageList messageList = new MessageList("message-list");
        Div div = new Div();
        Label label = new Label(mess);
        label.getStyle().set("margin-right", "15px");
        button = new Button("Elimina ", VaadinIcon.MINUS.create());
        button.setId("button");
        button.addClickListener(buttonClickEvent1 -> {
            //Inizializzazione container da usare in base al gruppo
            Div box4;
            Div wrapper4;
            VerticalLayout parolaLayout;
            if(!isTeacher){
                box4 = Utils.getDivFromListByAttribute(containersBox, "name", gruppo.getId());
                wrapper4 = Utils.getDivFromListByAttribute(containersWrapper, "name", gruppo.getId());
                parolaLayout = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutList, "name", gruppo.getId());
            }else{
                box4 = Utils.getDivFromListByAttribute(containersBoxTeacher, "name", gruppo.getId());
                wrapper4 = new Div();
                parolaLayout = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutTeacherList, "name", gruppo.getId());
            }

            parolaLayout.removeAll();
            if(isTeacher) {
                BroadcasterSuggerisciMaty.broadcast(mess, operazione, "Teacher", false, gruppo);
            }else{
                BroadcasterSuggerisciMaty.broadcast(mess, operazione, account.getNome(), false, gruppo);
            }

            box4.removeAll();
            wrapper4.removeAll();
            button.setEnabled(false);
            sendParola.setEnabled(true);
            int num = Integer.parseInt(mess);
            BroadcasterMaty.addIntegers(BroadcasterMaty.getIntegers().get(BroadcasterMaty.getIntegers().size() - 1) + num);

            box4.removeAll();
            wrapper4.removeAll();
            Div circle1 = new Div();
            circle1.addClassName("circle");
            circle1.setId("colorpad1");
            Paragraph paragraph1 = new Paragraph();
            paragraph1.addClassName("parag1");
            Span span1 = new Span("0");
            paragraph1.add(span1);
            circle1.add(paragraph1);
            box4.add(circle1);
            addDivToMainContent(box4, gruppo);
            //add(box4);

            if(!isTeacher) { //se non e' il teacher -> mostra animazione pallina
                Div d1 = new Div();
                d1.setWidth(null);
                Paragraph p1 = new Paragraph("0");
                p1.addClassName("parag2");
                d1.addClassName("paer");
                d1.setId("colorpad");
                d1.add(p1);
                wrapper4.add(d1);
                wrapper4.addClassName("box1");
                addDivToMainContent(wrapper4, gruppo);
                //add(wrapper4);
                getElement().executeJs("setRandomColor()");
            }
            checkIfWin();
        });
        //deleteButtonStyle();
        div.add(label, button);
        messageList.add(div);

        VerticalLayout parolaLayout;
        if(isTeacher){
            parolaLayout = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutTeacherList, "name", gruppo.getId());
        }else{
            parolaLayout = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutList, "name", gruppo.getId());
        }
        parolaLayout.add(messageList);
        if(Utils.isAccountInThisGruppo(gruppo, account)){
            parolaLayout.getStyle().set("display", "flex");
        }else{
            parolaLayout.getStyle().set("display", "none");
        }

        /*------FINE----------------------------------------------------------------------------------------------------------------------------------*/


        suggerisci.setValue("");
        sendParola.setEnabled(false);
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

        for(VerticalLayout i : parolaLayoutTeacherList){
            i.getStyle().set("display", "none");
        }

        for(VerticalLayout i : cronologiaNUmeriTeacherList){
            i.getStyle().set("display", "none");
        }
    }

    //Getter and setter
    public ArrayList<Div> getContainersBoxTeacher() {
        return containersBoxTeacher;
    }

    public ArrayList<VerticalLayout> getParolaLayoutTeacherList() {
        return parolaLayoutTeacherList;
    }

    public ArrayList<VerticalLayout> getCronologiaNUmeriTeacherList() {
        return cronologiaNUmeriTeacherList;
    }
}
