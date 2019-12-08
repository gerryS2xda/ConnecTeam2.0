package com.example.demo.utility;

import com.example.demo.games.Guess;
import com.example.demo.games.Maty;
import com.example.demo.userOperation.NavBarVertical;
import com.example.demo.users.broadcaster.Broadcaster;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@StyleSheet("frontend://stile/appbarStyle.css")
public class AppBarUI extends HorizontalLayout {

    //static field
    public static final String ICON_BTN_SIZE = "24px";
    public static final String APPBAR_HEIGHT = "2.75rem";

    //instance field
    private boolean isVerticalLayoutPage;
    private H4 textBar;
    private Div main;
    private Div appBarContainer;
    private boolean isGamePage;
    private String nameGame;

    public AppBarUI(String titlePage, boolean isVertPage, boolean isGamePage){
        isVerticalLayoutPage = isVertPage;
        this.isGamePage = isGamePage;
        this.nameGame = titlePage;  //NOTA: titolo pagina deve essere uguale al nome del gioco (evitare di usare altra variabile)

        //getStyle().set("margin-left", NavBarVertical.NAVBAR_WIDTH);
        getStyle().set("margin", "0");
        setSpacing(false);

        main = new Div();
        main.addClassName("app-bar");
        main.getStyle().set("height", APPBAR_HEIGHT);

        appBarContainer = new Div();
        appBarContainer.addClassName("app-bar__container");
        appBarContainer.getStyle().set("height", APPBAR_HEIGHT);

        textBar = new H4(titlePage);
        textBar.addClassName("app-bar__title");
        if(isVerticalLayoutPage)
            textBar.getStyle().set("margin-left", NavBarVertical.NAVBAR_WIDTH);
        if(isGamePage){
            appBarContainer.add(textBar, buttonContainer());
        }else {
            appBarContainer.add(textBar);
        }
        main.add(appBarContainer);

        add(main);
    }

    private HorizontalLayout buttonContainer(){
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.getStyle().set("position", "absolute");
        horizontalLayout.getStyle().set("left", "60%");

        DialogUtility dialogUtility = new DialogUtility();

        Icon info = new Icon(VaadinIcon.INFO_CIRCLE_O);
        info.setSize(ICON_BTN_SIZE);
        Button infoBtn = new Button("Info", info);
        infoBtn.setHeight(APPBAR_HEIGHT);
        infoBtn.getStyle().set("background-color", "#0000");
        infoBtn.addClickListener(buttonClickEvent -> {
            Dialog d = new Dialog();
            if(nameGame.equals("Guess")){
                d = dialogUtility.descrizioneGiocoDialog(new Guess());
            }else if(nameGame.equals("Maty")){
                d = dialogUtility.descrizioneGiocoDialog(new Maty());
            }
            d.open();
        });

        Icon close = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
        close.setSize(ICON_BTN_SIZE);
        Button terminateGame = new Button("Termina partita", close);
        terminateGame.setHeight(APPBAR_HEIGHT);
        terminateGame.getStyle().set("background-color", "#0000");
        terminateGame.addClickListener(buttonClickEvent -> {
           if(nameGame.equals("Guess")){
               com.example.demo.guess.gamesMenagemet.backend.broadcaster.Broadcaster.terminaPartitaFromTeacher();
               Broadcaster.setCountGuessUser(0); //reset counter giocatori di Guess
           }else if(nameGame.equals("Maty")){
               com.example.demo.maty.gameMenagement.backend.broadcaster.BroadcasterMaty.terminaPartitaFromTeacher();
               Broadcaster.setCountMatyUser(0); //reset counter giocatori di Maty
           }
        });

        horizontalLayout.add(infoBtn, terminateGame);
        return horizontalLayout;
    }


    //definire pulsante per aprire e chiudere la navbar vertical
    //definire un metodo statico per open and close nella NavBarVertical
}
