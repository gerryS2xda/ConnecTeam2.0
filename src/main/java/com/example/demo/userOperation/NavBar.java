package com.example.demo.userOperation;

import com.example.demo.mainView.MainView;
import com.example.demo.users.discusser.StudentHomeView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@HtmlImport("style.html")
@StyleSheet("frontend://stile/stile.css")
public class NavBar extends HorizontalLayout {

    //instance field
    private HorizontalLayout homeContainer;
    private HorizontalLayout settingsContainer;
    private HorizontalLayout statisticsContainer;
    private HorizontalLayout logOutContainer;

    //Questa barra di navigazione e' stata usata solo per i discusser
    public NavBar (){

        //Style nav bar orizzontale
        getStyle().set("background-color","#84c0c9");
        getStyle().set("padding","0px");
        getElement().getStyle().set("width", "100%");
        getStyle().set("border-radius","10%");
        UnorderedList unorderedList = new UnorderedList();
        HorizontalLayout main = new HorizontalLayout();
        main.getStyle().set("padding","10px");

        //Container per pulsante "Home" e link collegamento
        homeContainer = new HorizontalLayout();
        Icon homeIcon = new Icon(VaadinIcon.HOME);
        homeIcon.setSize("30px");
        homeIcon.setColor("#007d99");
        RouterLink routerLink = new RouterLink("Home", StudentHomeView.class);
        routerLink.getStyle().set("text-decoration", "none");
        routerLink.addClassName("router-link");
        unorderedList.add(routerLink);
        Div space = new Div();
        space.setWidth("100%");
        homeContainer.add(homeIcon,routerLink);

        //Container per pulsante "Impostazioni" e link collegamento
        settingsContainer = new HorizontalLayout();
        Icon settingIcon = new Icon(VaadinIcon.COGS);
        settingIcon.setSize("30px");
        settingIcon.setColor("#007d99");
        RouterLink routerLink1 = new RouterLink("Impostazioni", SettingsUser.class);
        routerLink1.getStyle().set("text-decoration", "none");
        routerLink1.addClassName("router-link");
        Div space1 = new Div();
        space1.setWidth("100%");
        settingsContainer.add(settingIcon,routerLink1);

        //Container per pulsante "Statistiche" e link collegamento
        statisticsContainer = new HorizontalLayout();
        Icon statisticheIcon = new Icon(VaadinIcon.PIE_BAR_CHART);
        statisticheIcon.setSize("30px");
        statisticheIcon.setSize("30px");
        statisticheIcon.setColor("#007d99");
        RouterLink routerLink2 = new RouterLink("Statistiche", StatisticUser.class);
        routerLink2.getStyle().set("text-decoration", "none");
        routerLink2.addClassName("router-link");  //style extra definito in stile.css
        Div space2 = new Div();
        space2.setWidth("100%");
        statisticsContainer.add(statisticheIcon,routerLink2);

        //Container per pulsante "Logout" e link collegamento
        logOutContainer = new HorizontalLayout();
        logOutContainer.getStyle().set("position","absolute");
        logOutContainer.getStyle().set("left","88%");
        logOutContainer.getStyle().set("top", "6px");

        Icon logoutIcon = new Icon(VaadinIcon.SIGN_OUT);
        logoutIcon.setSize("30px");
        logoutIcon.setColor("#007d99");
        logoutIcon.getStyle().set("margin-top", "6px");
        logoutIcon.getStyle().set("margin-right", "8px");

        Button routerLink3 = new Button("Logout");
        routerLink3.addClickListener(buttonClickEvent -> {
            VaadinSession.getCurrent().getSession().invalidate();
            UI.getCurrent().navigate(MainView.class);  //vai alla pagina "MainView" (classe con @Route("MainView")
            System.out.println(VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user"));
        });

        routerLink3.addClassName("buttonLogOut");
        Div space3 = new Div();
        space3.setWidth("100%");
        logOutContainer.add(logoutIcon,routerLink3);
        String styles = ".router-link { "
                + "margin-left: 10px;"
                + "font-size: 20px;"
                + " }";

        StreamRegistration resource = UI.getCurrent().getSession()
                .getResourceRegistry()
                .registerResource(new StreamResource("styles.css", () -> {
                    byte[] bytes = styles.getBytes(StandardCharsets.UTF_8);
                    return new ByteArrayInputStream(bytes);
                }));
        UI.getCurrent().getPage().addStyleSheet(
                "base://" + resource.getResourceUri().toString());

        main.add(homeContainer, settingsContainer, statisticsContainer, logOutContainer);  //aggingi i div alla nav bar
        add(main);  //aggiungi la navbar nella view generale

        //Using Browser Window Resize Events for responsive
        UI.getCurrent().getPage().addBrowserWindowResizeListener(browserWindowResizeEvent -> {
            System.out.println("NavBar- Responsive  width: " + browserWindowResizeEvent.getWidth() + " height:" + browserWindowResizeEvent.getHeight());
            loadResponsiveConfiguration(browserWindowResizeEvent.getWidth(), browserWindowResizeEvent.getHeight());
        });
    }

    private void loadResponsiveConfiguration(int widthBrowser, int heightBrowser){

        if(widthBrowser <= 1000){
            logOutContainer.getStyle().set("left", "80%");
        }else{
            logOutContainer.getStyle().set("position","absolute");
            logOutContainer.getStyle().set("left","88%");
            return;
        }

        if(widthBrowser <= 650){
            logOutContainer.getStyle().set("position", "initial");
        }else{
            logOutContainer.getStyle().set("position","absolute");
            logOutContainer.getStyle().set("left", "80%");
            return;
        }
    }
}
