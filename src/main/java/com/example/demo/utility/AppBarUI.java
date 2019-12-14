package com.example.demo.utility;

import com.example.demo.userOperation.NavBarVertical;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.H4;

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
        appBarContainer.add(textBar);
        main.add(appBarContainer);

        add(main);
    }

    //definire pulsante per aprire e chiudere la navbar vertical
    //definire un metodo statico per open and close nella NavBarVertical
}
