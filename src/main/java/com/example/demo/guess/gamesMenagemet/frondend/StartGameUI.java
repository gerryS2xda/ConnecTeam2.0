package com.example.demo.guess.gamesMenagemet.frondend;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.error.ErrorPage;
import com.example.demo.guess.gamesMenagemet.backend.GuessController;
import com.example.demo.guess.gamesMenagemet.backend.broadcaster.Broadcaster;
import com.example.demo.guess.gamesMenagemet.backend.broadcaster.BroadcasterSuggerisci;
import com.example.demo.guess.gamesMenagemet.backend.listeners.SuggerisciListener;
import com.example.demo.utility.MessageList;
import com.example.demo.utility.Utils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/style.css")
public class StartGameUI extends VerticalLayout implements SuggerisciListener{

    private VerticalLayout parolaLayout = new VerticalLayout();
    private Button button;
    private GuessController guessController;
    private Image logoGuess;
    private boolean vincente;
    private boolean flag = false;
    private boolean isTeacher;
    private Account account; //account che sta interagendo con il gioco
    private List<Gruppo> gruppi = new ArrayList<Gruppo>();
    private Gruppo g; //gruppo a cui appartiene questo account
    private Div azioni;

    public StartGameUI(GuessController guessController, boolean isTeacher, Account account) {

        try {
            gruppi = com.example.demo.users.broadcaster.Broadcaster.getGruppiListReceive();
            setId("StartGameUI");
            this.guessController = guessController;
            this.isTeacher = isTeacher;
            this.account = account;
            g = Utils.findGruppoByAccount(gruppi, account);
            azioni = new Div();
            azioni.setId("AzioniparolaSuggerita");


            BroadcasterSuggerisci.register(this);

            logoGuess = new Image("frontend/img/Guess.jpeg", "guess");
            logoGuess.setWidth("200px");
            logoGuess.setHeight("200px");

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.addClassName("horizontalLayoutStartGameUI");
            if(isTeacher){
                horizontalLayout.getStyle().set("top", "28%"); //valore precedente: 230px
            }

            TextField suggertisci = new TextField();
            Label label = new Label("Suggerisci una soluzione");
            label.addClassName("labelSuggerisci");
            if(isTeacher){
                getStyle().set("width", "60%");
                getStyle().set("height", "260px");
                label.getStyle().set("top", "20%");
                add(label);
            }else{
                add(label, logoGuess);
            }

            Button sendParola = new Button("Suggerisci");
            sendParola.addClickListener(buttonClickEvent -> {
                String mess = suggertisci.getValue();
                if (!mess.equals("")) {
                    BroadcasterSuggerisci.broadcast(suggertisci.getValue());
                    suggertisci.setValue("");
                }
            });

            Label paroleVotate = new Label("Parole votate: ");
            paroleVotate.addClassName("parolevotatelabel");
            if(isTeacher){
                paroleVotate.getStyle().set("top", "20%");
            }
            add(paroleVotate);

            sendParola.getStyle().set("cursor", "pointer");
            sendParola.setWidth(null);
            parolaLayout.addClassName("suggerisciParolaLayout");
            if(isTeacher){
                parolaLayout.getStyle().set("top", "30px");
                parolaLayout.getStyle().set("margin-left", "0px");
                parolaLayout.getStyle().set("padding-left", "0px");
                parolaLayout.setWidth("350px");
            }else{
                parolaLayout.setWidth("250px");
            }
            horizontalLayout.add(suggertisci, sendParola, parolaLayout);
            add(horizontalLayout);
        }

        catch (Exception e){
            removeAll();
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
            e.printStackTrace();
        }

    }


    @Override
    public Button receiveBroadcast(String message) {
        getUI().get().access(() -> {
            MessageList messageList = new MessageList("message-list");
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setId("paroleSuggeriteBtnPlus");

            Label label = new Label();  //parola suggerita
            label.setId("parolaSuggerita");
            label.getStyle().set("margin-top", "8px");
            label.setText(message);

            Icon icon = new Icon(VaadinIcon.PLUS);
            icon.setSize("24px");
            button = new Button(icon);

            button.addClickListener(buttonClickEvent -> {
                Broadcaster.addString(message);
                Map<String, Integer> stringIntegerMap = countFrequencies(Broadcaster.getStrings());
                Broadcaster.getVotoParola(stringIntegerMap);
                disableButton();

                stringIntegerMap.forEach((s, integer) -> {
                    if (integer == Broadcaster.getListeners().size()) {
                        for (int i = 0; i < Broadcaster.getPartiteThread().size(); i++) {
                            if (i == 0) {
                                flag = false;
                            }
                            try {
                                Broadcaster.getPartiteThread().get(i).interrupt();
                                Broadcaster.getPartiteThread().get(i).stopTimer();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            } finally {
                                int punteggio = 0;
                                if (Broadcaster.getIndiziRicevuti() == 1) {
                                    punteggio = 100;
                                } else if (Broadcaster.getIndiziRicevuti() == 2) {
                                    punteggio = 60;
                                } else if (Broadcaster.getIndiziRicevuti() == 3) {
                                    punteggio = 30;
                                } else if (Broadcaster.getIndiziRicevuti() == 4) {
                                    punteggio = 10;
                                }

                                vincente = guessController.partitaVincente(s, Broadcaster.getItems().get(i));

                                if (vincente == false && flag == false) {
                                    Broadcaster.partitanonVincente();
                                } else if (vincente == true && flag == false) {
                                    Broadcaster.partitaVincente(s, punteggio);
                                }
                            }

                        }
                    }
                });
            });

            button.getStyle().set("background-color","#007d99");
            button.getStyle().set("border-radius","30px 10px 30px 40px");
            button.getStyle().set("margin-left","30px");
            button.getStyle().set("cursor","pointer");
            button.getStyle().set("color","white");

            azioni.add(label, button);
            g.getAzioniAccount().put(account, azioni);

            //Solo ai membri dell'account a verra' mostrato la parola suggerita
            for(Account a : g.getMembri()){
                horizontalLayout.add(label, button);
                messageList.add(horizontalLayout);
                parolaLayout.add(messageList);
            }

            if(isTeacher){
                horizontalLayout.getStyle().set("width", "350px");
                icon.getStyle().set("left", "100px");
                button.getStyle().set("width", "32px");


            }

        });

        return button;
    }

    public Map<String, Integer> countFrequencies(ArrayList<String> list) {

        Map<String, Integer> hm = new HashMap<String, Integer>();
        for (String i : list) {
            Integer j = hm.get(i);
            hm.put(i, (j == null) ? 1 : j + 1);
        }

        /*for (Map.Entry<String, Integer> val : hm.entrySet()) {
            System.out.println("Element " + val.getKey() + " "
                    + "occurs"
                    + ": " + val.getValue() + " times");
        }*/

        return hm;
    }

    void disableButton(){
        button.setEnabled(false);
    }

}
