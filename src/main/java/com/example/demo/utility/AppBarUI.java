package com.example.demo.utility;

import com.example.demo.games.Guess;
import com.example.demo.games.Maty;
import com.example.demo.userOperation.NavBarVertical;
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
    private static final String ICON_BTN_SIZE = "16px";
    private static final String APPBAR_HEIGHT = "2.75rem";

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

        Dialog d = descrizioneGiocoDialog();
        Icon info = new Icon(VaadinIcon.INFO_CIRCLE_O);
        info.setSize(ICON_BTN_SIZE);
        Button infoBtn = new Button("Info", info);
        infoBtn.setHeight(APPBAR_HEIGHT);
        infoBtn.addClickListener(buttonClickEvent -> {
            d.open();
        });

        Icon close = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
        close.setSize(ICON_BTN_SIZE);
        Button terminateGame = new Button("Termina partita", close);
        terminateGame.setHeight(APPBAR_HEIGHT);


        horizontalLayout.add(infoBtn, terminateGame);
        return horizontalLayout;
    }

    private Dialog descrizioneGiocoDialog(){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);
        d.setWidth("640px");
        d.setHeight("320px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("height", "100%");

        Label title = new Label("Info su Guess");
        title.getStyle().set("font-size", "32px");

        Label descrizione = new Label();
        if(nameGame.equals("Guess")){
            Guess guess = new Guess();
            descrizione.setText(guess.getDescrizioneLungaGioco());
        }else if(nameGame.equals("Maty")){
            Maty maty = new Maty();
            descrizione.setText(maty.getDescrizioneLungaGioco());
        }
        descrizione.getStyle().set("font-size", "16px");

        Button cancelButton = new Button("Close");
        cancelButton.getStyle().set("background-color","#007d99");
        cancelButton.getStyle().set("cursor","pointer");
        cancelButton.getStyle().set("color","white");
        cancelButton.getStyle().set("margin-top", "50px");
        cancelButton.addClickListener(buttonClickEvent -> {
            d.close();
        });
        content.add(title, descrizione, cancelButton);

        d.add(content);
        return d;
    }

    //definire pulsante per aprire e chiudere la navbar vertical
    //definire un metodo statico per open and close nella NavBarVertical
}
