package com.example.demo.users.controller;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.error.ErrorPage;
import com.example.demo.userOperation.NavBarVertical;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import java.util.ArrayList;
import java.util.List;


@HtmlImport("style.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/gestStudStyle.css")
public class GestioneStudentUIGuess extends HorizontalLayout {

    //instance field
    private int numeroGruppi = 0; //numeri di gruppi di creare
    private String nomeGioco = ""; //nome del gioco che si sta gestendo
    private List<Grid<Account>> gridGruppi;
    private List<Gruppo> gruppi;
    private Label title;
    private HorizontalLayout containerChangeUI;
    private GestioneStudentUI mainUI;

    public GestioneStudentUIGuess(GestioneStudentUI gestioneStudentUI){

        try {
            //Inizializzazione
            gridGruppi = new ArrayList<Grid<Account>>();
            gruppi = new ArrayList<Gruppo>();
            getElement().setAttribute("id", "GestioneStudentUIGuess");
            mainUI = gestioneStudentUI;

            VerticalLayout containerTitleGuide = new VerticalLayout();
            containerTitleGuide.setPadding(false);
            containerTitleGuide.setSpacing(false);
            containerTitleGuide.addClassName("containerTitleGuide");
            containerTitleGuide.setWidth("80%");
            containerTitleGuide.getStyle().set("left", NavBarVertical.NAVBAR_WIDTH);

            title = new Label("");
            title.addClassName("titleStyle");
            title.getElement().setAttribute("id", "titleGuess");

            Paragraph guidetxt = new Paragraph("Selezionate uno studente dalla lista e trascinatelo nel gioco che desiderate." +
                    " Premere il pulsante 'Gioca' per iniziare una nuova partita con il gruppo di studenti che è stato impostato.");

            containerTitleGuide.add(title, guidetxt);

            add(containerTitleGuide);

            containerChangeUI = new HorizontalLayout();
            containerChangeUI.addClassName("changeUIContainerBtn");

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public Tabs createGridsAndGroups(){

        Tabs tabs = new Tabs();
        tabs.setWidth("75%");
        tabs.getStyle().set("padding", "0");

        for(int i = 0, j=i+1; i < numeroGruppi; i++, j++){
            Grid<Account> grid = new Grid<>(Account.class);
            grid.removeAllColumns();
            grid.getElement().setAttribute("id", "Gruppo" + j);
            grid.addColumn(Account::getNome).setHeader("Gruppo " + j);
            grid.getStyle().set("width", "100%");
            grid.getStyle().set("height", "80%");
            gridGruppi.add(grid);

            Gruppo g = new Gruppo();
            g.setId("Gruppo "+j);
            g.setNomeGioco(nomeGioco);
            gruppi.add(g);

            VerticalLayout vert = new VerticalLayout();
            vert.getElement().setAttribute("id", "gridContainer"+j);
            vert.addClassName("gridContainerVertLayout");
            vert.setSpacing(false);
            vert.add(gridGruppi.get(i));
            vert.setWidth("230px"); //default: 100%
            vert.setHeight("100%");

            Tab t = new Tab(vert);
            t.getElement().setAttribute("id","Gruppo" + j);
            t.getStyle().set("padding", "0");
            tabs.add(t);

        }
        tabs.getElement().setAttribute("id", "tabsGUESS");
        return tabs;
    }

    public void showBtnForChangeUI() {

        mainUI.setCurrentGameShow("Guess");

        if(containerChangeUI.getElement().getAttribute("id") != null){
            if(containerChangeUI.getElement().getAttribute("id").equals("containerChangeUIMaty")){
                return; //containerChangeUI gia' creato e aggiunto alla UI
            }
        }

        if (mainUI.getMainMaty().getElement().getAttribute("name") != null) {
            if (mainUI.getMainMaty().getElement().getAttribute("name").equals("createdMaty")) { //verifica se la UI maty e' stata creata

                containerChangeUI.getElement().setAttribute("id", "containerChangeUIMaty");

                Button b = new Button("Maty");
                b.addClassName("changeUIBtnStyle");
                b.addClickListener(buttonClickEvent -> {
                    mainUI.getMainGuess().getStyle().set("display", "none");
                    mainUI.getMainMaty().getStyle().set("display", "flex");

                    mainUI.getContainerGridGuess().getStyle().set("display", "none");
                    mainUI.getContainerGridMaty().getStyle().set("display", "flex");

                    mainUI.getGestioneStudentUIMaty().showBtnForChangeUI();
                });
                Icon ic = new Icon(VaadinIcon.CHEVRON_RIGHT);
                ic.setSize("24px");
                ic.addClassName("changeUIIcon");
                containerChangeUI.add(b, ic);

                add(containerChangeUI);
            }
        }
    }

    //Getter and setter
    public void setNumeroGruppi(int numeroGruppi) {
        this.numeroGruppi = numeroGruppi;
    }

    public void setNomeGioco(String nomeGioco) {
        this.nomeGioco = nomeGioco;
    }

    public List<Grid<Account>> getGridGruppi() {
        return gridGruppi;
    }

    public List<Gruppo> getGruppi() {
        return gruppi;
    }

    public void setTitleLabel(String txt){
        title.setText(txt);
    }

    public HorizontalLayout getContainerChangeUIMaty() {
        return containerChangeUI;
    }
}
