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
import com.example.demo.utility.DialogUtility;
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
public class StudentHomeView extends HorizontalLayout implements BroadcastListener, BeforeLeaveObserver {

    private Account account;
    private PartitaRepository partitaRepository;
    private Image imageU;  //immagine del profilo di un utente
    private AccountRepository accountRepository;
    private AccountListEventBeanPublisher accountEventListpublisher;
    private VerticalLayout main;
    private Paragraph msgAttesa;
    private boolean isShowErrorDialog = false;
    private Label nomeGiocatore;

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

            String actualWebBrowser = VaadinSession.getCurrent().getBrowser().getBrowserApplication();
            //verifica se l'account che sta tentando di accedere e' gia' loggato su un altro browser
            if (checkIfAccountIsAlreadyLogged(actualWebBrowser)) {  //utilizza confronto tra browser attuale e quello memorizzato al primo accesso
                return; //necessario, altrimenti viene caricata la pagina anche se mostra il Dialog
            }

            UI.getCurrent().getElement().getStyle().set("overflow", "hidden"); //access al <body> element

            NavBar navBar = new NavBar(false);
            add(navBar);

            main = new VerticalLayout();
            main.addClassName("main1");
            main.getStyle().set("top", "140px"); //'spazio' tra navbar e main content (valore precedente: 80px)

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
            Broadcaster.addNewAccountWithWebBrowser(account, VaadinSession.getCurrent().getBrowser());

            accountEventListpublisher.doStuffAndPublishAnEvent(Broadcaster.getAccountList(), "add"); //publish a new event for GestStudentUI
            UI.getCurrent().setPollInterval(3000);

            //Using Browser Window Resize Events for responsive
            UI.getCurrent().getPage().addBrowserWindowResizeListener(browserWindowResizeEvent -> {
                loadResponsiveConfiguration(browserWindowResizeEvent.getWidth(), browserWindowResizeEvent.getHeight());
            });
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
    private boolean checkIfAccountIsAlreadyLogged(String actualWebBrowser){
        isShowErrorDialog = false;

        for (Account i : Broadcaster.getAccountWithWebBrowserHashMap().keySet()) {
             if (i.equals(account)) {   //se e' presente un account nella hashmap
                 String str = Broadcaster.getAccountWithWebBrowserHashMap().get(i).getBrowserApplication();
                 if(!actualWebBrowser.equals(str)) { //se il browser associato ad 'account' e' diverso da quello nella hashmap ad 'i'
                     //'account' proviene da un altro browser
                     DialogUtility dialogUtility = new DialogUtility();
                     dialogUtility.showErrorDialog("Errore", "L'utente che sta tentando di accedere al sito e' gia' loggato su un altro web browser!", "red");
                     isShowErrorDialog = true;
                     break;
                 } //else: se sono uguali significa che si sta eseguendo un refresh della pagina
             }
        }
        return isShowErrorDialog;
    }

    private HorizontalLayout homeUser() {

        HorizontalLayout main = new HorizontalLayout();
        main.addClassName("positioning");
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(false);
        verticalLayout.addClassName("positioning2");
        HorizontalLayout layoutWelcome = new HorizontalLayout();
        layoutWelcome.addClassName("banner");
        String welcome = "";

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

        nomeGiocatore = new Label(welcome + account.getNome());
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

    private void loadResponsiveConfiguration(int widthBrowser, int heightBrowser){

        if(widthBrowser <= 600 || heightBrowser <= 480){
            imageU.setWidth("80px");
            imageU.setHeight("80px");
            msgAttesa.getStyle().set("font-size", "18px");
            nomeGiocatore.getStyle().set("font-size", "2em");
        }else{
            imageU.setWidth("170px");
            imageU.setHeight("170px");
            msgAttesa.getStyle().set("font-size", "20px");
            nomeGiocatore.getStyle().set("font-size", "3em");
        }
    }

    //Implements methods of BeforeLeaveObserver
    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        System.out.println("StudentHomeView: beforeLeave() is invoking");
        if(isShowErrorDialog)
            return; //Se viene mostrato il dialog di errore -> esci da questo metodo

        Map<Account, String> tempList = new HashMap<>();    //invia solo account da cancellare tramite HashMap
        tempList.put(account, "remove");
        accountEventListpublisher.doStuffAndPublishAnEvent(tempList, "remove"); //invia event per rimozione account da accountList event inviato in precedenza

        Broadcaster.unregister(account, this);
        Broadcaster.removeAccountWithWebBrowser(account);
        UI.getCurrent().setPollInterval(-1);
    }

    //Implements methods of BroadcastListener interface
    @Override
    public void redirectToGuess(){
        System.out.println("StudentHomeView - redirectToGuess: ui " + getUI().get());
        if(!getUI().isPresent()){
            showErrorPage();
        }else {
            getUI().get().access(() -> {
                getUI().get().getPage().executeJs("window.location.href = \"http://localhost:8080/guess\";");
            });
        }
    }

    @Override
    public void redirectToMaty(){
        System.out.println("StudentHomeView - redirectToMaty: ui " + getUI().get());
        if(!getUI().isPresent()){
            showErrorPage();
        }else {
            getUI().get().access(() -> {
                getUI().get().getPage().executeJs("window.location.href = \"http://localhost:8080/maty\";");
            });
        }
    }

}
