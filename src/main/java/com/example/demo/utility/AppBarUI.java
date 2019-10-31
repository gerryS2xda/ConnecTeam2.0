package com.example.demo.utility;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.H4;

@StyleSheet("frontend://stile/appbarStyle.css")
public class AppBarUI extends HorizontalLayout {

    public AppBarUI(String titlePage){
        //getStyle().set("margin-left", NavBarVertical.NAVBAR_WIDTH);
        getStyle().set("margin", "0");
        setSpacing(false);

        Div main = new Div();
        main.addClassName("app-bar");

        Div main2 = new Div();
        main2.addClassName("app-bar__container");

        /* Style del pulsante per aprire e chiudere nav bar
            <vaadin-button class="app-bar__navi-icon" theme="tertiary-inline icon" aria-label="Menu" style="line-height: 1;" tabindex="0" role="button">
				<iron-icon icon="vaadin:menu" slot="prefix"></iron-icon>
			</vaadin-button>
         */
        H4 h4 = new H4(titlePage);
        h4.addClassName("app-bar__title");
        main2.add(h4); //aggiungere style pulsante
        main.add(main2);

        add(main);
    }


    //definire pulsante per aprire e chiudere la navbar vertical
    //definire un metodo statico per open and close nella NavBarVertical
}
