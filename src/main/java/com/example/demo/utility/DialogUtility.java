package com.example.demo.utility;

import com.example.demo.mainView.MainView;
import com.example.demo.games.maty.frondend.MatyUI;
import com.example.demo.users.controller.TeacherMainUITab;
import com.example.demo.users.discusser.StudentHomeView;
import com.example.demo.entity.Account;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.gamesManagement.Game;
import com.example.demo.games.guess.frondend.GuessUI;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.server.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@HtmlImport("style.html")
@StyleSheet("frontend://stile/style.css")
public class DialogUtility extends VerticalLayout {

    //static field
    private static final String LOGO_GAME_SIZE = "100px";


    //instance field
    private String control= "";

    public DialogUtility(){
        //vuoto
    }

    public void loginDialog(Account account){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);
        d.setWidth("100%");
        d.setHeight("100%");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("height", "100%");

        Label titleLab = new Label("Registrazione completata");
        titleLab.getStyle().set("font-size", "24px");
        titleLab.getStyle().set("color", "black");

        Button accediBtn = new Button("Accedi");
        accediBtn.getStyle().set("background-color", "#007d99");
        accediBtn.getStyle().set("cursor", "pointer");
        accediBtn.getStyle().set("color", "white");
        accediBtn.getStyle().set("margin-top", "25px");
        accediBtn.addClickShortcut(Key.ENTER);
        accediBtn.addClickListener(buttonClickEvent -> {
            d.close();
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loggato", true);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("userId", account.getId());
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("user", account);

            if(account.getTypeAccount().equals("teacher")){
                UI.getCurrent().navigate(TeacherMainUITab.class);
            }else{
                UI.getCurrent().navigate(StudentHomeView.class);
            }
        });
        content.add(titleLab, accediBtn);

        d.add(content);
        d.open();
    }

    public void passwordDimenticata(){

        AccountRepository accountRepository = (AccountRepository) VaadinRequest.getCurrent().getWrappedSession().getAttribute("rep");

        Dialog d = new Dialog();
        d.setWidth("100%");
        d.setHeight("100%");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.START);
        content.getStyle().set("height", "100%");

        Label titleLab = new Label("Password dimenticata");
        titleLab.getStyle().set("font-size", "24px");
        titleLab.getStyle().set("align-self" , "center");
        titleLab.getStyle().set("margin-bottom", "16px");

        Label descrizione = new Label();
        descrizione.getStyle().set("font-size", "16px");
        descrizione.setText("Inserisci l'email e se è presente, puoi impostare una nuova password");

        HorizontalLayout hor1 = new HorizontalLayout();
        TextField email = new TextField("Email");
        Button sendMail = new Button("Invia");
        sendMail.getStyle().set("align-self", "flex-end");
        sendMail.setEnabled(false);
        hor1.add(email, sendMail);

        content.add(titleLab, descrizione, hor1);

        Binder<String> binder = new Binder<>();
        binder.setBean(control);
        Label validationStatus = new Label();
        binder.setStatusLabel(validationStatus);
        binder.forField(email)
                .asRequired("Inserisci L'e-mail")
                .withValidator(new EmailValidator("Indirizzo e-mail non valido"))
                .bind(s -> control, (s, v) -> control = v);
        binder.withValidator(Validator.from(account ->{
            if(email.isEmpty()){
                d.remove(validationStatus);
                return true;
            }else{
                Account a = accountRepository.findOneByEmail(email.getValue());
                if(a!=null){
                    return true;
                }else{
                    Div space = new Div();
                    space.setWidth("100%");
                    d.add(space,validationStatus);
                    return false;
                }
            }
        },"Email non esistente" ));
        binder.addStatusChangeListener(
                event -> sendMail.setEnabled(binder.isValid()));
        sendMail.addClickListener(clickEvent -> {
            d.close();
            Random rand = new Random();
            int n = rand.nextInt(9000) + 1000;
            accountRepository.updatePassword(email.getValue(), n + "");

            //Nuova implementazione
            showRecoveryPasswordDialog(n + "").open();

            /*  Questa implementazione e' stata rimossa, vedi ConnecTeam1.0 per il vecchio codice
                SendMail.sendMailTLS(email.getValue(), "Cambia password", "La nuova password è: " + n);
                InfoEventUtility infoEventUtility =  new InfoEventUtility();
                infoEventUtility.infoEvent("Email Inviata con successo","115");
             */
        });

        d.add(content);
        d.open();
    }

    private Dialog showRecoveryPasswordDialog(String newPwd){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);
        d.setWidth("400px");
        d.setHeight("100%");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("height", "100%");

        Label titleLab = new Label("Recupero password");
        titleLab.getStyle().set("font-size", "24px");
        titleLab.getStyle().set("margin-bottom", "16px");

        Label descrizione = new Label();
        descrizione.getStyle().set("font-size", "16px");
        descrizione.getStyle().set("align-self", "start");
        descrizione.setText("La sua nuova password e': " + newPwd);

        Button cancelButton = new Button("Close");
        cancelButton.getStyle().set("background-color", "#007d99");
        cancelButton.getStyle().set("cursor", "pointer");
        cancelButton.getStyle().set("color", "white");
        cancelButton.getStyle().set("margin-top", "25px");
        cancelButton.addClickListener(buttonClickEvent -> {
            d.close();
        });
        content.add(titleLab, descrizione, cancelButton);
        d.add(content);

        return d;
    }

    public void partitaVincente(String parola, int punteggio, Game game){
        Dialog dialog = new Dialog();
        dialog.setWidth("100%"); //min width: 600px (da impostare)
        dialog.setMinHeight("420px");
        dialog.setHeight("100%");
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        VerticalLayout content = new VerticalLayout();
        Image image;
        Label label;

        if(game.getNomeGioco() == "Guess") {
            image = new Image("frontend/img/Guess.jpeg", "guess");
            image.setWidth("200px");
            image.setHeight("200px");
            label = new Label("Hai  indovinato la parola: " + parola);
            label.getStyle().set("font-size", "30px");


        }else {
            image = new Image("frontend/img/Maty.jpeg", "maty");
            image.setWidth("200px");
            image.setHeight("200px");
            label = new Label("Hai  trovato la soluzione: " + parola);
            label.getStyle().set("font-size", "30px");

        }

        Label punti = new Label("Hai ottenuto: " + punteggio + " punti");
        punti.getStyle().set("font-size", "30px");
        Button listGiochi = new Button("Vai alla sala di attesa");
        listGiochi.addClassName("my-style2");
        content.addClassName("my-style");
        content.setAlignItems(Alignment.CENTER);
        String styles = ".my-style { "
                + " }"
                + " .my-style2 { "
                + "cursor: pointer;"
                + "background-color: #007d99;"
                + "color: white;"
                + " }";
        StreamRegistration resource = UI.getCurrent().getSession()
                .getResourceRegistry()
                .registerResource(new StreamResource("styles.css", () -> {
                    byte[] bytes = styles.getBytes(StandardCharsets.UTF_8);
                    return new ByteArrayInputStream(bytes);
                }));
        UI.getCurrent().getPage().addStyleSheet(
                "base://" + resource.getResourceUri().toString());
        listGiochi.addClickListener(buttonClickEvent -> {
            if(game.getNomeGioco().equals("Guess")){
                GuessUI.reset();
            }else if(game.getNomeGioco().equals("Maty")){
                MatyUI.reset();
            }
            dialog.close();
            UI.getCurrent().navigate(StudentHomeView.class);
            UI.getCurrent().getPage().reload(); //da aggiungere quando si è su pc o browser diversi
        });
        content.add(image,label,punti,listGiochi);
        dialog.add(content);
        dialog.open();
    }

    public void partitanonVincente(Game game){
        Label punti;
        Dialog dialog = new Dialog();
        dialog.setWidth("100%");    //min width: 600px (da impostare)
        dialog.setMinHeight("420px");
        dialog.setHeight("100%");
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        VerticalLayout content = new VerticalLayout();
        Image image;
        Label label;

        if(game.getNomeGioco().equals("Guess")){
            punti = new Label("Hai ottenuto: "+0+ " punti");
            image = new Image("frontend/img/Guess.jpeg", "guess");
            image.setWidth("200px");
            image.setHeight("200px");
            label = new Label("Hai perso, ritenta!");
            label.getStyle().set("font-size","30px");
            punti.getStyle().set("font-size","30px");
        }else {
            punti = new Label("Hai comunque ottenuto: "+5+ " punti");
            image = new Image("frontend/img/Maty.jpeg", "maty");
            image.setWidth("200px");
            image.setHeight("200px");
            label = new Label("Dai riprova! Impegnatevi di più la prossima volta!");
            Image image1 = new Image("frontend/img/smile.png", "smile");
            image1.setHeight("50px");
            image1.setWidth("70px");
            label.add(image1);
            label.getStyle().set("font-size","30px");
            punti.getStyle().set("font-size","30px");
        }

        HorizontalLayout btnContainer = new HorizontalLayout();
        Button listGiochi = new Button("Vai alla sala di attesa");
        listGiochi.addClassName("my-style2");

        Button closePage = new Button("Chiudi pagina");
        closePage.addClassName("my-style2");

        content.addClassName("my-style");
        content.setAlignItems(Alignment.CENTER);

        String styles = ".my-style { "
                + " }"
                + " .my-style2 { "
                + "cursor: pointer;"
                + "background-color: #007d99;"
                + "color: white;"
                + " }";
        StreamRegistration resource = UI.getCurrent().getSession()
                .getResourceRegistry()
                .registerResource(new StreamResource("styles.css", () -> {
                    byte[] bytes = styles.getBytes(StandardCharsets.UTF_8);
                    return new ByteArrayInputStream(bytes);
                }));
        UI.getCurrent().getPage().addStyleSheet(
                "base://" + resource.getResourceUri().toString());

        listGiochi.addClickListener(buttonClickEvent -> {
            if(game.getNomeGioco().equals("Guess")){
                GuessUI.reset();
            }else if(game.getNomeGioco().equals("Maty")){
                MatyUI.reset();
            }
            dialog.close();
            UI.getCurrent().navigate(StudentHomeView.class);
            UI.getCurrent().getPage().reload(); //da aggiungere quando si è su pc o browser diversi
        });

        btnContainer.add(listGiochi);

        content.add(image,label,punti,btnContainer);
        dialog.add(content);
        dialog.open();
    }

    public void partitaTerminataForAll(String msg){

        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        Label label = new Label(msg);
        Button button = new Button("Vai alla home");
        button.addClickListener(buttonClickEvent -> {
            dialog.close();
            UI.getCurrent().navigate(StudentHomeView.class);
            UI.getCurrent().getPage().reload();
        });
        button.getStyle().set("margin-left","20px");
        dialog.add(label,button);
        dialog.open();

    }

    public void partitaTerminataDialogTeacher(String title, String motivazione){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);
        d.setWidth("100%");
        d.setHeight("100%");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("height", "100%");

        Label titleLab = new Label(title);
        titleLab.getStyle().set("font-size", "32px");
        titleLab.getStyle().set("color", "black");

        Label descrizione = new Label();
        descrizione.getStyle().set("font-size", "16px");
        descrizione.getStyle().set("color", "black");
        descrizione.setText(motivazione);

        Button cancelButton = new Button("Close");
        cancelButton.getStyle().set("background-color", "#007d99");
        cancelButton.getStyle().set("cursor", "pointer");
        cancelButton.getStyle().set("color", "white");
        cancelButton.getStyle().set("margin-top", "25px");
        cancelButton.addClickListener(buttonClickEvent -> {
            d.close();
        });
        content.add(titleLab, descrizione, cancelButton);

        d.add(content);
        d.open();
    }

    public Dialog descrizioneGiocoDialog(Game game, Image logoGame){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);
        d.setWidth("640px");
        d.setHeight("100%"); //valore precedente: 320px

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("height", "100%");

        Label title = new Label("Info su " + game.getNomeGioco());
        title.getStyle().set("font-size", "32px");
        title.getStyle().set("color", "black");

        logoGame.setWidth(LOGO_GAME_SIZE);
        logoGame.setHeight(LOGO_GAME_SIZE);
        logoGame.getStyle().set("margin-top", "8px");
        logoGame.getStyle().set("margin-left", "8px");

        Label descrizione = new Label();
        descrizione.setText(game.getDescrizioneLungaGioco());
        descrizione.getStyle().set("font-size", "16px");
        descrizione.getStyle().set("color", "black");

        Button cancelButton = new Button("Close");
        cancelButton.getStyle().set("background-color", "#007d99");
        cancelButton.getStyle().set("cursor", "pointer");
        cancelButton.getStyle().set("color", "white");
        cancelButton.addClickListener(buttonClickEvent -> {
            d.close();
        });
        content.add(title, logoGame, descrizione, cancelButton);

        d.add(content);
        return d;
    }

    public void showErrorDialog(String title, String motivazione, String colorTxt){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);
        d.setWidth("100%");
        d.setHeight("100%");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("height", "100%");

        Label titleLab = new Label(title);
        titleLab.getStyle().set("font-size", "32px");
        titleLab.getStyle().set("color", colorTxt);

        Label descrizione = new Label();
        descrizione.getStyle().set("font-size", "16px");
        descrizione.getStyle().set("color", colorTxt);
        descrizione.setText(motivazione);

        Button cancelButton = new Button("Vai alla Home");
        cancelButton.getStyle().set("background-color", "#007d99");
        cancelButton.getStyle().set("cursor", "pointer");
        cancelButton.getStyle().set("color", "white");
        cancelButton.getStyle().set("margin-top", "25px");
        cancelButton.addClickListener(buttonClickEvent -> {
            d.close();
            VaadinSession.getCurrent().getSession().invalidate();  //chiudi la sessione utente corrente
            UI.getCurrent().navigate(MainView.class);  //vai alla pagina "MainView" (classe con @Route("MainView")
            UI.getCurrent().getPage().reload(); //da aggiungere quando si è su pc o browser diversi
        });
        content.add(titleLab, descrizione, cancelButton);

        d.add(content);
        d.open();
    }

    public Dialog showDialog(String text, String colorTxt){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);
        d.setWidth("100%");
        d.setHeight("100%");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("height", "100%");

        Label descrizione = new Label();
        descrizione.getStyle().set("font-size", "16px");
        descrizione.getStyle().set("color", colorTxt);
        descrizione.setText(text);

        content.add(descrizione);

        d.add(content);
        return d;
    }

    public Dialog showConfirmDialogForGame(String title, String msg, boolean isLogout){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setWidth("100%");
        d.setHeight("100%");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("height", "100%");

        Label titleLab = new Label(title);
        titleLab.getStyle().set("font-size", "32px");
        titleLab.getStyle().set("color", "black");

        Label descrizione = new Label();
        descrizione.getStyle().set("font-size", "16px");
        descrizione.getStyle().set("color", "black");
        descrizione.setText(msg);

        HorizontalLayout btnContainer = new HorizontalLayout();
        btnContainer.getStyle().set("margin-top", "30px");
        Button positive = new Button("OK");
        positive.getStyle().set("background-color", "#007d99");
        positive.getStyle().set("cursor", "pointer");
        positive.getStyle().set("color", "white");
        positive.addClickListener(buttonClickEvent -> {
            d.close();
            if(isLogout){
                VaadinSession.getCurrent().getSession().invalidate();  //chiudi la sessione utente corrente
                UI.getCurrent().navigate(MainView.class);  //vai alla pagina "MainView" (classe con @Route("MainView")
            }else {
                UI.getCurrent().navigate(StudentHomeView.class);  //vai alla pagina "StudentHomeView"
            }
            UI.getCurrent().getPage().reload(); //da aggiungere quando si è su pc o browser diversi
        });

        Button negative = new Button("Continua partita");
        negative.getStyle().set("background-color", "#007d99");
        negative.getStyle().set("cursor", "pointer");
        negative.getStyle().set("color", "white");
        negative.addClickListener(buttonClickEvent -> {
            d.close();
        });

        btnContainer.add(positive, negative);

        content.add(titleLab, descrizione, btnContainer);
        d.add(content);
        return d;
    }

    public Dialog showDialogPartitaInCorso(String title, String msg){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);
        d.setWidth("100%");
        d.setHeight("100%");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("height", "100%");

        Label titleLab = new Label(title);
        titleLab.getStyle().set("font-size", "32px");
        titleLab.getStyle().set("color", "black");

        Label descrizione = new Label();
        descrizione.getStyle().set("font-size", "16px");
        descrizione.getStyle().set("color", "black");
        descrizione.setText(msg);

        HorizontalLayout btnContainer = new HorizontalLayout();
        btnContainer.getStyle().set("margin-top", "30px");
        Button positive = new Button("OK");
        positive.getStyle().set("background-color", "#007d99");
        positive.getStyle().set("cursor", "pointer");
        positive.getStyle().set("color", "white");
        positive.addClickListener(buttonClickEvent -> {
            d.close();
            UI.getCurrent().navigate(StudentHomeView.class);  //vai alla pagina "StudentHomeView"
            UI.getCurrent().getPage().reload(); //da aggiungere quando si è su pc o browser diversi
        });

        btnContainer.add(positive);

        content.add(titleLab, descrizione, btnContainer);
        d.add(content);

        return d;
    }
}
