package com.example.demo.users.controller;

import com.example.demo.entity.Account;
import com.example.demo.entity.Partita;
import com.example.demo.entity.Punteggio;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.gamesManagement.GameList;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.example.demo.utility.AppBarUI;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.List;


@Route("ControllerHomeView")
@HtmlImport("style.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/navBarVertStyle.css")
@JavaScript("frontend://js/script.js")
@PageTitle("ConnecTeam")
public class ControllerMainUI extends HorizontalLayout {

    private Account account;
    private PartitaRepository partitaRepository;
    private GameList gameList;
    private Image image;
    private AccountRepository accountRepository;

    public ControllerMainUI() {

        try{
            accountRepository = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
            account = (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
            if(accountRepository == null || account == null){
                showErrorPage();
                return;
            }
            if(!account.getTypeAccount().equals("teacher")){
                throw new IllegalArgumentException("Questo account non puo' accedere a questa pagina");
            }

            UI.getCurrent().getElement().getStyle().set("overflow", "hidden"); //access al <body> element
            getStyle().set("height", "100%"); //per nav bar verticale

            AppBarUI appBar = new AppBarUI("Home", false, false); //nome pagina corrente
            add(appBar);

            VerticalLayout main1 = new VerticalLayout();
            main1.addClassName("main1");
            main1.getStyle().set("margin-left", "252px"); //margin left for nav bar vertical
            main1.getStyle().set("left", "-10%");
            main1.getStyle().set("right", "0");
            main1.getStyle().set("top", "140px");
            VerticalLayout main = new VerticalLayout();
            main.setWidth(null);
            main.add(homeUser());
            main1.add(main);
            add(main1);

        }catch (Exception e){
            showErrorPage();
            e.printStackTrace();
        }

    }

    private void showErrorPage(){
        removeAll();
        ErrorPage errorPage = new ErrorPage();
        add(errorPage);
    }

    private HorizontalLayout homeUser() {

        HorizontalLayout main = new HorizontalLayout();
        main.addClassName("positioning");
        partitaRepository = (PartitaRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("partitaRepository");
        gameList = (GameList) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("gameList");
        VerticalLayout verticalLayout;
        verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(false);
        verticalLayout.addClassName("positioning2");
        HorizontalLayout layoutWelcome = new HorizontalLayout();
        layoutWelcome.addClassName("banner");
        String welcome=null;

        if(account.getSesso().equals("1")){
            welcome = "Benvenuta ";
            if(account.getProfilePicture()==null){
                image = new Image("frontend/img/profiloGirl.png", "foto profilo");
                image.addClassName("welcomeProfileImg");
                image.setWidth("170px");
                image.setHeight("170px");
            }else {
                welcome = "Benvenuta ";
                image = generateImage(account);
                image.addClassName("welcomeProfileImg");
                image.setWidth("170px");
                image.setHeight("170px");
            }

        }else if (account.getSesso().equals("0")) {
            if(account.getProfilePicture()==null){
                welcome = "Benvenuto ";
                image = new Image("frontend/img/profiloBoy.png", "foto profilo");
                image.addClassName("welcomeProfileImg");
                image.setWidth("170px");
                image.setHeight("170px");
            }else {
                welcome = "Benvenuto ";
                image = generateImage(account);
                image.addClassName("welcomeProfileImg");
                image.setWidth("170px");
                image.setHeight("170px");
            }
        }

        VerticalLayout layoutNomeEPartita = new VerticalLayout();
        layoutNomeEPartita.add(image);

        Label nomeGiocatore = new Label(welcome + account.getNome());
        layoutNomeEPartita.addClassName("layoutNomeEPartita");
        layoutNomeEPartita.add(nomeGiocatore);
        nomeGiocatore.addClassName("welcomeLabel");

        Partita partita = partitaRepository.lastPartita(account);
        System.out.println("Partita: " + partita);


        if (partita == null) {
            Label label1 = new Label("Non hai ancora effettuato una partita");
            layoutNomeEPartita.add(label1);
        } else {

            VerticalLayout partitaInfo = new VerticalLayout();
            partitaInfo.addClassName("WelcomePartita");
            String s = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(partita.getTimestamp());
            Label ultimaPartita = new Label("Ultima Partita: "+partita.getGioco() + " " + s);
            List<Punteggio> punteggi = partita.getArray();
            int n = 0;
            for (Punteggio p : punteggi) {
                if (p.getAccount().getId() == account.getId()) {
                    n = p.getPunti();
                    break;
                }
            }
            Label punti = new Label("Punteggio: " + n);
            punti.getStyle().set("margin","0");
            partitaInfo.add(ultimaPartita, punti);
            layoutNomeEPartita.add(partitaInfo);
        }
        layoutWelcome.add(layoutNomeEPartita);
        verticalLayout.add(layoutWelcome);
        main.add(verticalLayout);

        return main;
    }

    public Image generateImage(Account account) {
        Long id = account.getId();
        StreamResource sr = new StreamResource("user", () ->  {
            Account attached = accountRepository.findWithPropertyPictureAttachedById(id);
            return new ByteArrayInputStream(attached.getProfilePicture());
        });
        sr.setContentType("image/png");
        Image image = new Image(sr, "profile-picture");
        return image;
    }

}
