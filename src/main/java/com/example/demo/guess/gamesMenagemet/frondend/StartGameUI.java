package com.example.demo.guess.gamesMenagemet.frondend;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.error.ErrorPage;
import com.example.demo.guess.gamesMenagemet.backend.GuessController;
import com.example.demo.guess.gamesMenagemet.backend.broadcaster.BroadcasterGuess;
import com.example.demo.guess.gamesMenagemet.backend.broadcaster.BroadcasterSuggerisci;
import com.example.demo.guess.gamesMenagemet.backend.listeners.SuggerisciListener;
import com.example.demo.utility.MessageList;
import com.example.demo.utility.Utils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
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
@StyleSheet("frontend://stile/guessStyle.css")
public class StartGameUI extends VerticalLayout implements SuggerisciListener{

    //instance field
    private GuessController guessController;
    private Image logoGuess;
    private boolean vincente;
    private boolean flag = false;
    private boolean isTeacher;
    private Account account; //account che sta interagendo con il gioco
    private List<Gruppo> gruppi = new ArrayList<Gruppo>();
    private ArrayList<VerticalLayout> parolaLayoutList;
    private ArrayList<VerticalLayout> parolaLayoutTeacherList;


    public StartGameUI(GuessController guessController, boolean isTeacher, Account account) {

        try {
            gruppi = com.example.demo.users.broadcaster.Broadcaster.getGruppiListReceive();
            setId("StartGameUI");
            this.guessController = guessController;
            this.isTeacher = isTeacher;
            this.account = account;
            parolaLayoutList = new ArrayList<VerticalLayout>();
            parolaLayoutTeacherList = new ArrayList<VerticalLayout>();

            BroadcasterSuggerisci.register(account, this);

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
                    if(isTeacher){
                        BroadcasterSuggerisci.broadcast(Utils.findGruppoByName(gruppi, GuessUI.currentGroupSelect.getId()), suggertisci.getValue());
                    }else {
                        BroadcasterSuggerisci.broadcast(Utils.findGruppoByAccount(gruppi, account), suggertisci.getValue());
                    }
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

            horizontalLayout.add(suggertisci, sendParola);

            for(Gruppo x : gruppi){
                VerticalLayout vert = new VerticalLayout();
                vert.getElement().setAttribute("name", x.getId());
                vert.addClassName("suggerisciParolaLayout");
                vert.getStyle().set("display", "none");
                parolaLayoutList.add(vert);

                VerticalLayout vert2 = new VerticalLayout();
                vert2.getElement().setAttribute("name", x.getId());
                vert2.addClassName("suggerisciParolaLayoutTeacher");
                vert2.getStyle().set("display", "none");
                parolaLayoutTeacherList.add(vert2);

                horizontalLayout.add(vert, vert2);
            }

            add(horizontalLayout);
        }

        catch (Exception e){
            removeAll();
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
            e.printStackTrace();
        }

    }


    //NOTA: Primo listener e' il teacher e quindi: gruppo.getId() e' una stringa vuota
    @Override
    public void receiveBroadcast(Gruppo gruppo, String message) {  //gruppo da cui e' stata 'suggerita' la parola (serve per teacher)
        if(isTeacher){
            receiveBroadcastTeacher(gruppo, message);
            return;
        }

        getUI().get().access(() -> {
            VerticalLayout parolaLayout = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutList, "name", gruppo.getId());

            MessageList messageList = new MessageList("message-list");
            HorizontalLayout horizontalLayout = new HorizontalLayout();

            Label label = new Label();  //parola suggerita
            label.addClassName("labelParolaSuggerita");
            label.setText(message);

            Icon icon = new Icon(VaadinIcon.PLUS);
            icon.setSize("24px");
            Button button = new Button(icon);
            button.addClassName("btnPlus");

            button.addClickListener(buttonClickEvent -> {
                BroadcasterGuess.addParolaVotata(gruppo, message);
                Map<String, Integer> stringIntegerMap = countFrequencies(BroadcasterGuess.getParoleVotateHM().get(gruppo));
                BroadcasterGuess.getVotoParola(gruppo, stringIntegerMap);
                button.setEnabled(false);

                stringIntegerMap.forEach((s, integer) -> {
                    if (integer == BroadcasterGuess.getListeners().size()) {
                        for (int i = 0; i < BroadcasterGuess.getPartiteThread().size(); i++) {
                            if (i == 0) {
                                flag = false;
                            }
                            try {
                                BroadcasterGuess.getPartiteThread().get(i).interrupt();
                                BroadcasterGuess.getPartiteThread().get(i).stopTimer();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                int punteggio = 0;
                                if (BroadcasterGuess.getIndiziRicevuti() == 1) {
                                    punteggio = 100;
                                } else if (BroadcasterGuess.getIndiziRicevuti() == 2) {
                                    punteggio = 60;
                                } else if (BroadcasterGuess.getIndiziRicevuti() == 3) {
                                    punteggio = 30;
                                } else if (BroadcasterGuess.getIndiziRicevuti() == 4) {
                                    punteggio = 10;
                                }

                                vincente = guessController.partitaVincente(s, BroadcasterGuess.getItems().get(i));

                                if (vincente == false && flag == false) {
                                    BroadcasterGuess.partitanonVincente();
                                } else if (vincente == true && flag == false) {
                                    BroadcasterGuess.partitaVincente(s, punteggio);
                                }
                            }

                        }
                    }
                });
            });

            //Solo ai membri del gruppo in cui si trova account 'a' verra' mostrato la parola suggerita
            for(Account a : gruppo.getMembri()){
                horizontalLayout.add(label, button);
                messageList.add(horizontalLayout);
                parolaLayout.add(messageList);
            }

            if(Utils.isAccountInThisGruppo(gruppo, account)){
                parolaLayout.getStyle().set("display", "flex");
            }else{
                parolaLayout.getStyle().set("display", "none");
            }

        });

    }

    /*NOTA: Necessario clonare gli oggetti perche' se usiamo horizontalLayout.add(label, button); e horizontalLayoutTeacher.add(label, button);
     *  e poi impostiamo il display.none a messageListTeacher, accade che vengono resi invisibili 'label e button' di horizontalLayoutTeacher
     *  ma anche quelli di horizontalLayout (NB: vaadin non implementa un cloner)
     */
    public void receiveBroadcastTeacher(Gruppo gruppo, String message) {
        getUI().get().access(() -> {
            VerticalLayout parolaLayoutTeacher = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutTeacherList, "name", gruppo.getId());

            MessageList messageListTeacher = new MessageList("message-list");
            HorizontalLayout horizontalLayoutTeacher = new HorizontalLayout();
            horizontalLayoutTeacher.getStyle().set("width", "350px");

            Label label = new Label();  //parola suggerita
            label.addClassName("labelParolaSuggerita");
            label.setText(message);

            Icon icon = new Icon(VaadinIcon.PLUS);
            icon.setSize("24px");
            icon.getStyle().set("left", "100px");

            Button button = new Button(icon);
            button.addClassName("btnPlus");
            button.setWidth("32px");
            button.addClickListener(buttonClickEvent -> {
                BroadcasterGuess.addParolaVotata(gruppo, message);
                Map<String, Integer> stringIntegerMap = countFrequencies(BroadcasterGuess.getParoleVotateHM().get(gruppo));
                BroadcasterGuess.getVotoParola(gruppo, stringIntegerMap);
                button.setEnabled(false);

                stringIntegerMap.forEach((s, integer) -> {
                    if (integer == BroadcasterGuess.getListeners().size()) {
                        for (int i = 0; i < BroadcasterGuess.getPartiteThread().size(); i++) {
                            if (i == 0) {
                                flag = false;
                            }
                            try {
                                BroadcasterGuess.getPartiteThread().get(i).interrupt();
                                BroadcasterGuess.getPartiteThread().get(i).stopTimer();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                int punteggio = 0;
                                if (BroadcasterGuess.getIndiziRicevuti() == 1) {
                                    punteggio = 100;
                                } else if (BroadcasterGuess.getIndiziRicevuti() == 2) {
                                    punteggio = 60;
                                } else if (BroadcasterGuess.getIndiziRicevuti() == 3) {
                                    punteggio = 30;
                                } else if (BroadcasterGuess.getIndiziRicevuti() == 4) {
                                    punteggio = 10;
                                }

                                vincente = guessController.partitaVincente(s, BroadcasterGuess.getItems().get(i));

                                if (vincente == false && flag == false) {
                                    BroadcasterGuess.partitanonVincente();
                                } else if (vincente == true && flag == false) {
                                    BroadcasterGuess.partitaVincente(s, punteggio);
                                }
                            }

                        }
                    }
                });
            });

            horizontalLayoutTeacher.add(label, button);
            messageListTeacher.getElement().setAttribute("id", "PL"+gruppo.getId());
            messageListTeacher.add(horizontalLayoutTeacher);
            parolaLayoutTeacher.add(messageListTeacher);

        });

    }

    public Map<String, Integer> countFrequencies(List<String> list) {

        Map<String, Integer> hm = new HashMap<String, Integer>();
        for (String i : list) {
            Integer j = hm.get(i);
            hm.put(i, (j == null) ? 1 : j + 1);
        }

        return hm;
    }



    public void showParolaSuggeritaAndBtnTeacher(String currentGroupId){

        for(VerticalLayout i : parolaLayoutTeacherList){ //Nascondi tutti i parolaLayoutTeacherList e mostra solo quelli del currentGroupId
            i.getStyle().set("display", "none");
        }

        VerticalLayout vert = Utils.getVerticalLayoutFromListByAttribute(parolaLayoutTeacherList, "name", currentGroupId);
        vert.getStyle().set("display", "flex");
    }
}
