package com.example.demo.userOperation;

import com.example.demo.mainView.MainView;
import com.example.demo.users.controller.GestioneStudentUI;
import com.example.demo.users.controller.ControllerMainUI;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

@StyleSheet("frontend://stile/navBarVertStyle.css")
public class NavBarVertical extends VerticalLayout{

    //costanti
    private static final String icon_Size = "24px"; //valore precedente: 30px
    private static final String icon_color = "#007d99";
    public static final String NAVBAR_WIDTH = "252px";
    public static final String NAVBAR_HEIGHT = "100%";

    public NavBarVertical(){
        getStyle().set("width", NAVBAR_WIDTH);
        getStyle().set("height", NAVBAR_HEIGHT);
        getStyle().set("margin", "0px");
        getStyle().set("padding", "0px");
        addClassName("navi-drawer__content");

        Div navScrollArea = new Div();
        navScrollArea.addClassName("navi-drawer__scroll-area");
        navScrollArea.getStyle().set("width", "100%");

        Div items = new Div(); //container di tutti gli item della navbar
        items.getStyle().set("margin-bottom", "0.5rem");
        items.getStyle().set("margin-top", "0.5 rem");

        Icon homeIcon = new Icon(VaadinIcon.HOME);
        homeIcon.setSize(icon_Size);
        homeIcon.setColor(icon_color);
        Div home = addDivContainerItem("Home", ControllerMainUI.class, homeIcon);

        Icon settingIcon = new Icon(VaadinIcon.COGS);
        settingIcon.setSize(icon_Size);
        settingIcon.setColor(icon_color);
        Div settings = addDivContainerItem("Settings", SettingsUser.class, settingIcon);

        Icon gestStudIcon = new Icon(VaadinIcon.EDIT);
        gestStudIcon.setSize(icon_Size);
        gestStudIcon.setColor(icon_color);
        Div gestStud = addDivContainerItem("Gestione Studenti", GestioneStudentUI.class, gestStudIcon);

        Icon gamesIcon = new Icon(VaadinIcon.GAMEPAD);
        gamesIcon.setSize(icon_Size);
        gamesIcon.setColor(icon_color);
        Div gamesStud = addDivContainerItem("Giochi", SettingsUser.class, gamesIcon);

        items.add(home, settings, gestStud, gamesStud, logoutItem());
        navScrollArea.add(items);
        add(addLogoContainer(), navScrollArea);
    }

    public Div addDivContainerItem(String linkText, Class<? extends Component> navigationTarget, Icon ic){
        Div d = new Div();
        d.addClassName("navi-item");
        RouterLink rl = new RouterLink("", navigationTarget);
        rl.addClassName("navi-item__link");
        Span sp = new Span(linkText);
        rl.add(ic, sp);
        rl.addClassName("highlight");

        d.add(rl);
        return d;
    }

    public Div addLogoContainer(){
        Div d = new Div();
        d.addClassName("brand-expression");
        d.getStyle().set("width", "100%");
        Image img = new Image("icon.png", "ConnectTeam logo");
        img.getStyle().set("max-height", "100%");
        img.getStyle().set("max-width", "100%");
        Label lab = new Label("ConnecTeam");
        lab.addClassNames("custom_h3", "brand-expression__title");
        d.add(img, lab);
        return d;
    }

    public Div logoutItem(){
        Div d = new Div();
        Icon logoutIcon = new Icon(VaadinIcon.SIGN_OUT);
        logoutIcon.setSize(icon_Size);
        logoutIcon.setColor(icon_color);
        d.addClassName("navi-item");
        d.getStyle().set("cursor", "pointer");
        RouterLink rl = new RouterLink();
        rl.addClassName("navi-item__link_logout");
        Span sp = new Span("Logout");
        rl.add(logoutIcon, sp);

        d.add(rl);
        d.addClickListener(event -> {
            VaadinSession.getCurrent().getSession().invalidate();  //chiudi la sessione utente corrente
            UI.getCurrent().navigate(MainView.class);  //vai alla pagina "MainView" (classe con @Route("MainView")
        });
        return d;
    }

}
