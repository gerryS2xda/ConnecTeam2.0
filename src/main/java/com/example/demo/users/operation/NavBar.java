package com.example.demo.users.operation;

import com.example.demo.mainView.MainView;
import com.example.demo.users.discusser.StudentHomeView;
import com.example.demo.utility.DialogUtility;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@HtmlImport("style.html")
@StyleSheet("frontend://stile/stile.css")
public class NavBar extends HorizontalLayout {

    //static field
    public static final String NAVBAR_HORIZONTAL_HEIGHT = "56px";

    //instance field
    private HorizontalLayout main;
    private HorizontalLayout homeContainer;
    private HorizontalLayout homeContainerWithBtn;
    private HorizontalLayout settingsContainer;
    private HorizontalLayout statisticsContainer;
    private HorizontalLayout infoGameContainer;
    private HorizontalLayout chatContainer; //verra' settato in GuessUI/MatyUI
    private HorizontalLayout logOutContainer;
    private boolean isGamePage;

    //Questa barra di navigazione e' stata usata solo per i discusser
    public NavBar (boolean isGamePage){

        this.isGamePage = isGamePage;

        //Style nav bar orizzontale
        getStyle().set("background-color","#84c0c9");
        getStyle().set("padding","0px");
        setHeight(NAVBAR_HORIZONTAL_HEIGHT);
        getElement().getStyle().set("width", "100%");

        main = new HorizontalLayout();
        main.getStyle().set("padding","10px");

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

        chatContainer = new HorizontalLayout();
        infoGameContainer = new HorizontalLayout();

        if(isGamePage){
            main.add(homeContainerWithBtn(), infoGameContainer, chatContainer, logOutContainer());
        }else{
            main.add(homeContainer(), settingsContainer(), statisticsContainer(), logOutContainer());  //aggingi i div alla nav bar
        }

        add(main);  //aggiungi la navbar nella view generale

        //Using Browser Window Resize Events for responsive
        UI.getCurrent().getPage().addBrowserWindowResizeListener(browserWindowResizeEvent -> {
            loadResponsiveConfiguration(browserWindowResizeEvent.getWidth(), browserWindowResizeEvent.getHeight());
        });
    }

    private HorizontalLayout homeContainer(){
        //Container per pulsante "Home" e link collegamento
        homeContainer = new HorizontalLayout();
        Icon homeIcon = new Icon(VaadinIcon.HOME);
        homeIcon.setSize("30px");
        homeIcon.setColor("#007d99");
        RouterLink routerLink = new RouterLink("Home", StudentHomeView.class);
        routerLink.getStyle().set("text-decoration", "none");
        routerLink.addClassName("router-link");
        homeContainer.add(homeIcon,routerLink);
        return homeContainer;
    }

    private HorizontalLayout homeContainerWithBtn(){
        //Container per pulsante "Home" (usato per Game)
        homeContainerWithBtn = new HorizontalLayout();
        Icon homeIcon = new Icon(VaadinIcon.HOME);
        homeIcon.setSize("30px");
        homeIcon.setColor("#007d99");

        //NOTA: Quando la partita termina, viene rimosso tutto il contenuto della pagina e quindi la navbar non viene mostrata
        Button routerLink3 = new Button("Home");
        routerLink3.addClickListener(buttonClickEvent -> {
            DialogUtility dialogUtility = new DialogUtility();
            dialogUtility.showConfirmDialogForGame("Partita in corso", "Sei sicuro di voler abbandonare la partita in corso?", false).open();
        });

        routerLink3.addClassName("buttonLogOut");
        homeContainerWithBtn.add(homeIcon,routerLink3);
        return homeContainerWithBtn;
    }

    private HorizontalLayout settingsContainer(){
        //Container per pulsante "Impostazioni" e link collegamento
        settingsContainer = new HorizontalLayout();
        Icon settingIcon = new Icon(VaadinIcon.COGS);
        settingIcon.setSize("30px");
        settingIcon.setColor("#007d99");
        RouterLink routerLink1 = new RouterLink("Impostazioni", SettingsUser.class);
        routerLink1.getStyle().set("text-decoration", "none");
        routerLink1.addClassName("router-link");
        settingsContainer.add(settingIcon,routerLink1);
        return settingsContainer;
    }

    private HorizontalLayout statisticsContainer(){
        //Container per pulsante "Statistiche" e link collegamento
        statisticsContainer = new HorizontalLayout();
        Icon statisticheIcon = new Icon(VaadinIcon.PIE_BAR_CHART);
        statisticheIcon.setSize("30px");
        statisticheIcon.setSize("30px");
        statisticheIcon.setColor("#007d99");
        RouterLink routerLink2 = new RouterLink("Statistiche", StatisticUser.class);
        routerLink2.getStyle().set("text-decoration", "none");
        routerLink2.addClassName("router-link");  //style extra definito in stile.css
        statisticsContainer.add(statisticheIcon,routerLink2);
        return statisticsContainer;
    }

    private HorizontalLayout logOutContainer(){
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
            if(isGamePage){
                DialogUtility dialogUtility = new DialogUtility();
                dialogUtility.showConfirmDialogForGame("Partita in corso", "Sei sicuro di voler abbandonare la partita in corso?", true).open();
            }else {
                VaadinSession.getCurrent().getSession().invalidate();
                UI.getCurrent().navigate(MainView.class);  //vai alla pagina "MainView" (classe con @Route("MainView")
                UI.getCurrent().getPage().reload(); //da aggiungere quando si è su pc o browser diversi
            }
        });

        routerLink3.addClassName("buttonLogOut");
        logOutContainer.add(logoutIcon,routerLink3);
        return logOutContainer;
    }

    //Getter and Setter
    public HorizontalLayout getChatContainer() {
        return chatContainer;
    }

    public HorizontalLayout getInfoGameContainer() {
        return infoGameContainer;
    }

    public HorizontalLayout getHomeContainerWithBtn() {
        return homeContainerWithBtn;
    }

    public HorizontalLayout getLogOutContainer(){
        return logOutContainer;
    }

    //Responsive methods
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
