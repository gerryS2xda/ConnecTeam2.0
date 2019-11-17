package com.example.demo.utility;

import com.example.demo.userOperation.NavBarVertical;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.H4;

@StyleSheet("frontend://stile/appbarStyle.css")
public class AppBarUI extends HorizontalLayout {

    private boolean isVerticalLayoutPage;

    public AppBarUI(String titlePage, boolean isVertPage){
        isVerticalLayoutPage = isVertPage;
        //getStyle().set("margin-left", NavBarVertical.NAVBAR_WIDTH);
        getStyle().set("margin", "0");
        setSpacing(false);

        Div main = new Div();
        main.addClassName("app-bar");

        Div main2 = new Div();
        main2.addClassName("app-bar__container");

        H4 textBar = new H4(titlePage);
        textBar.addClassName("app-bar__title");
        if(isVerticalLayoutPage)
            textBar.getStyle().set("margin-left", NavBarVertical.NAVBAR_WIDTH);
        main2.add(textBar); //aggiungere style pulsante
        main.add(main2);

        add(main);
    }

    //definire pulsante per aprire e chiudere la navbar vertical
    //definire un metodo statico per open and close nella NavBarVertical
}
