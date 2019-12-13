package com.example.demo.users.controller;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.error.ErrorPage;
import com.example.demo.userOperation.NavBarVertical;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import java.util.ArrayList;
import java.util.List;

@HtmlImport("style.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/gestStudStyle.css")
public class GestioneStudentUIMaty extends HorizontalLayout {

    //instance field
    private int numeroGruppi = 0; //numeri di gruppi di creare
    private String nomeGioco = ""; //nome del gioco che si sta gestendo
    private List<Grid<Account>> gridGruppi;
    private List<Gruppo> gruppi;
    private Label title;

    public GestioneStudentUIMaty(){

        try {
            //Inizializzazione
            gridGruppi = new ArrayList<Grid<Account>>();
            gruppi = new ArrayList<Gruppo>();
            getElement().setAttribute("id", "GestioneStudentUIMaty");

            VerticalLayout containerTitleGuide = new VerticalLayout();
            containerTitleGuide.setPadding(false);
            containerTitleGuide.setSpacing(false);
            containerTitleGuide.addClassName("containerTitleGuide");
            containerTitleGuide.setWidth("80%");
            containerTitleGuide.getStyle().set("left", NavBarVertical.NAVBAR_WIDTH);

            title = new Label("");
            title.addClassName("titleStyle");
            title.getElement().setAttribute("id", "titleMaty");

            Paragraph guidetxt = new Paragraph("Selezionate uno studente dalla lista e trascinatelo nel gioco che desiderate." +
                    " Premere il pulsante 'Gioca' per iniziare una nuova partita con il gruppo di studenti che è stato impostato.");

            containerTitleGuide.add(title, guidetxt);
            add(containerTitleGuide);

            //UI.getCurrent().setPollInterval(5000); Da usare solo le pagina viene caricata con UI.navigate(...)
        }catch (Exception e){
            removeAll();
            getStyle().set("background-color","white");
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
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
        tabs.getElement().setAttribute("id", "tabsMATY");
        return tabs;
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
}
