package com.example.demo.users.discusser;

import com.example.demo.userOperation.NavBar;
import com.example.demo.entity.Account;
import com.example.demo.entity.Partita;
import com.example.demo.entity.Punteggio;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.users.event.AccountListEventBeanPublisher;
import com.example.demo.users.broadcaster.BroadcastListener;
import com.example.demo.users.broadcaster.Broadcaster;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.*;


@Push
@Route("StudentHomeView")
@HtmlImport("style.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/style.css")
@JavaScript("frontend://js/script.js")
@PageTitle("ConnecTeam")
public class StudentHomeView extends HorizontalLayout implements BroadcastListener, PageConfigurator {

    private Account account;
    private PartitaRepository partitaRepository;
    private Image imageU;  //immagine del profilo di un utente
    private AccountRepository accountRepository;
    private int numeroUtenti = 0; //numero utenti connessi
    private AccountListEventBeanPublisher accountEventListpublisher;
    private boolean isStartPartita = false; //verifica se il teacher ha avviato la partita
    private VerticalLayout main;
    private Paragraph msgAttesa;

    public StudentHomeView(@Autowired AccountListEventBeanPublisher accountEventPublisher) {

        try{
            accountEventListpublisher = accountEventPublisher;

            accountRepository = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
            if(accountRepository == null)
                throw new IllegalArgumentException("StudentHomeView: AccountRepository is null");
            account = (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
            if(account == null)
                throw new IllegalArgumentException("StudentHomeView: Account is null");
            partitaRepository = (PartitaRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("partitaRepository");
            if(partitaRepository == null)
                throw new IllegalArgumentException("StudentHomeView: PartitaRepository is null");

            setId("StudentHomeView");

            NavBar navBar = new NavBar();
            add(navBar);

            main = new VerticalLayout();
            main.addClassName("main1");
            main.getStyle().set("top", "80px"); //'spazio' tra navbar e main content

            VerticalLayout userInfo = new VerticalLayout();
            userInfo.setWidth(null);
            userInfo.setSpacing(false);
            userInfo.setPadding(false);
            userInfo.add(homeUser());

            msgAttesa = new Paragraph("In attesa che il teacher inizi la sessione. Attendere...");
            msgAttesa.getStyle().set("font-size", "20px");

            main.add(userInfo, msgAttesa);
            add(main);

            Broadcaster.register(account, this);

            accountEventListpublisher.doStuffAndPublishAnEvent(Broadcaster.getAccountList()); //publish a new event for GestStudentUI
            UI.getCurrent().setPollInterval(3000);
        }catch (Exception e){
            showErrorPage();
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }

    public void showErrorPage(){
        removeAll();
        getStyle().set("background-color","white");
        ErrorPage errorPage = new ErrorPage();
        add(errorPage);
    }

    //private methods
    private HorizontalLayout homeUser() {

        HorizontalLayout main = new HorizontalLayout();
        main.addClassName("positioning");
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(false);
        verticalLayout.addClassName("positioning2");
        HorizontalLayout layoutWelcome = new HorizontalLayout();
        layoutWelcome.addClassName("banner");
        String welcome=null;

        if(account.getSesso().equals("1")){
            welcome = "Benvenuta ";
            if(account.getProfilePicture()==null){
                imageU = new Image("frontend/img/profiloGirl.png", "foto profilo");
                imageU.addClassName("welcomeProfileImg");
                imageU.setWidth("170px");
                imageU.setHeight("170px");
            }else {
                welcome = "Benvenuta ";
                imageU = generateImage(account);
                imageU.addClassName("welcomeProfileImg");
                imageU.setWidth("170px");
                imageU.setHeight("170px");
            }

        }else if (account.getSesso().equals("0")) {
            if(account.getProfilePicture()==null){
                welcome = "Benvenuto ";
                imageU = new Image("frontend/img/profiloBoy.png", "foto profilo");
                imageU.addClassName("welcomeProfileImg");
                imageU.setWidth("170px");
                imageU.setHeight("170px");
            }else {
                welcome = "Benvenuto ";
                imageU = generateImage(account);
                imageU.addClassName("welcomeProfileImg");
                imageU.setWidth("170px");
                imageU.setHeight("170px");
            }
        }

        VerticalLayout layoutNomeEPartita = new VerticalLayout();
        layoutNomeEPartita.add(imageU);

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

    //Implements methods of PageConfigurator
    @Override
    public void configurePage(InitialPageSettings initialPageSettings) {
        String script = "window.onbeforeunload = function (e) " +
                "{ var e = e || window.event; document.getElementById(\"StudentHomeView\").$server.browserIsLeaving(); return; };";
        initialPageSettings.addInlineWithContents(InitialPageSettings.Position.PREPEND, script, InitialPageSettings.WrapMode.JAVASCRIPT);
    }

    @ClientCallable
    public void browserIsLeaving() {  //se si esce dalla pagina corrente, invoca questo metodo
        System.out.println("browserInLeaving() is invoking");
        try {
            Broadcaster.getListeners().forEach((account1, broadcastListener) -> {
                if (account1.equals(account)) {
                    Broadcaster.unregister(account, this);
                    accountEventListpublisher.doStuffAndPublishAnEvent(Broadcaster.getAccountList()); //invia event per rimozione account da accountList event inviato in precedenza
                }
            });
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    //Implements methods of BroadcastListener interface
    @Override
    public void redirectToGuess(){
        System.out.println("StudentHomeView - redirectToGuess: ui " + getUI().get());
            getUI().get().access(() -> {
                System.out.println("StudentHomeView - redirectToGuess: a: " + account.toString());
                getUI().get().getPage().executeJs("window.location.href = \"http://localhost:8080/guess\";");
            });

    }

    @Override
    public void redirectToMaty(){
        if(getUI().get() == null){
            showErrorPage();
        }else {
            getUI().get().access(() -> {
                getUI().get().getPage().executeJs("window.location.href = \"http://localhost:8080/maty\";");
            });
        }
    }

    @Override
    public void updateAndMergeAccountList(){
        //No-implement
    }

    @Override
    public void startGameInBackground(String game){
        //no-implement
    }
}
