package com.example.demo.utility;

import com.example.demo.userOperation.NavBarVertical;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.H4;

@StyleSheet("frontend://stile/appbarStyle.css")
public class AppBarUI extends HorizontalLayout {

    //instance field
    private boolean isVerticalLayoutPage;
    private H4 textBar;
    private Div main;
    private Div appBarContainer;

    public AppBarUI(String titlePage, boolean isVertPage){
        isVerticalLayoutPage = isVertPage;
        //getStyle().set("margin-left", NavBarVertical.NAVBAR_WIDTH);
        getStyle().set("margin", "0");
        setSpacing(false);

        main = new Div();
        main.addClassName("app-bar");

        appBarContainer = new Div();
        appBarContainer.addClassName("app-bar__container");

        textBar = new H4(titlePage);
        textBar.addClassName("app-bar__title");
        if(isVerticalLayoutPage)
            textBar.getStyle().set("margin-left", NavBarVertical.NAVBAR_WIDTH);
        appBarContainer.add(textBar); //aggiungere style pulsante
        main.add(appBarContainer);

        add(main);
    }

    public void setTitleInAppBar(String titlePage, boolean isVertPage){
        H4 old = textBar;
        H4 nuovatxt = new H4(titlePage);
        nuovatxt.addClassName("app-bar__title");
        if(isVertPage)
            nuovatxt.getStyle().set("margin-left", NavBarVertical.NAVBAR_WIDTH);
        replace(old, nuovatxt);
        textBar = nuovatxt;
    }

    //definire pulsante per aprire e chiudere la navbar vertical
    //definire un metodo statico per open and close nella NavBarVertical
}
