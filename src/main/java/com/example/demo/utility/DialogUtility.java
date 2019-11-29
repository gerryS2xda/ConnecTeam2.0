package com.example.demo.utility;


import com.example.demo.games.Guess;
import com.example.demo.games.Maty;
import com.example.demo.users.discusser.StudentHomeView;
import com.example.demo.entity.Account;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.gamesRules.Game;
import com.example.demo.guess.gamesMenagemet.frondend.GuessUI;
import com.example.demo.users.controller.ControllerMainUI;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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

    private String control= "";

    public DialogUtility(){

    }

    public void loginDialog(Account account){
        Dialog dialog = new Dialog();
        Div content = new Div();
        Button accedi = new Button("Accedi");
        accedi.addClassName("my-style2");
        content.addClassName("my-style");
        content.setText("Registrazione completata");
        String styles = ".my-style { "
                + "margin-left: 60px;"
                + " }"
                + " .my-style2 { "
                + "margin-top: 27px;"
                + "margin-left: 110px;"
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
        dialog.setWidth("300px");
        dialog.setHeight("120px");
        dialog.add(content,accedi);
        dialog.open();

        accedi.addClickShortcut(Key.ENTER);

        accedi.addClickListener(clickEvent -> {
            dialog.close();
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loggato", true);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("userId", account.getId());
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("user", account);

            if(account.getTypeAccount().equals("teacher")){
                UI.getCurrent().navigate(ControllerMainUI.class);
            }else{
                UI.getCurrent().navigate(StudentHomeView.class);
            }

        });
    }

    public void passwordDimenticata(){

        Dialog dialog = new Dialog();
        Label label= new Label("Inserisci l'email, ti manderemo una password provvisoria");
        AccountRepository accountRepository = (AccountRepository) VaadinRequest.getCurrent().getWrappedSession().getAttribute("rep");
        FormLayout form= new FormLayout();
        TextField email= new TextField("Email");
        Button sendMail= new Button("Invia");
        sendMail.setEnabled(false);
        dialog.setWidth("400px");
        dialog.setHeight("120px");
        dialog.open();
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
                dialog.remove(validationStatus);
                return true;
            }else{
                Account a = accountRepository.findOneByEmail(email.getValue());
                if(a!=null){
                    return true;
                }else{
                    Div space = new Div();
                    space.setWidth("100%");
                    dialog.add(space,validationStatus);
                    return false;
                }
            }
        },"Email non esistente" ));
        binder.addStatusChangeListener(
                event -> sendMail.setEnabled(binder.isValid()));
        sendMail.addClickListener(clickEvent -> {
            Random rand = new Random();
            int n = rand.nextInt(9000) + 1000;
            accountRepository.updatePassword(email.getValue(), n + "");
            SendMail.sendMailTLS(email.getValue(), "Cambia password", "La nuova password è: " + n);
            InfoEventUtility infoEventUtility =  new InfoEventUtility();
            infoEventUtility.infoEvent("Email Inviata con successo","115");
            dialog.close();
        });
        dialog.add(label,form,email,sendMail);
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
        Button listGiochi = new Button("Vai alla lista giochi");
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
            GuessUI.reset();
            UI.getCurrent().navigate(ControllerMainUI.class);
            dialog.close();
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

        if(game.getNomeGioco() == "Guess"){
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
            GuessUI.reset();
            UI.getCurrent().navigate(StudentHomeView.class);
            dialog.close();
            UI.getCurrent().getPage().reload(); //da aggiungere quando si è su pc o browser diversi
        });

        closePage.addClickListener(buttonClickEvent -> {
            GuessUI.reset();
            dialog.close();
            UI.getCurrent().getPage().executeJs("window.close();");
        });

        btnContainer.add(listGiochi, closePage);

        content.add(image,label,punti,btnContainer);
        dialog.add(content);
        dialog.open();
    }

    public void partitaTerminata(Account account){

        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        Label label = new Label("Partita terminata da: "+account.getNome());
        Button button = new Button("Vai alla home");
        button.addClickListener(buttonClickEvent -> {
            UI.getCurrent().navigate(StudentHomeView.class);
            dialog.close();
            UI.getCurrent().getPage().reload();
        });
        button.getStyle().set("margin-left","20px");
        dialog.add(label,button);
        dialog.open();

    }

    public void partitaTerminataFromTeacher(){

        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        Label label = new Label("Partita terminata dal teacher");
        Button button = new Button("Vai alla home");
        button.addClickListener(buttonClickEvent -> {
            UI.getCurrent().navigate(StudentHomeView.class);
            dialog.close();
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

        Label descrizione = new Label();
        descrizione.getStyle().set("font-size", "16px");
        descrizione.setText(motivazione);

        Button cancelButton = new Button("Close");
        cancelButton.getStyle().set("background-color","#007d99");
        cancelButton.getStyle().set("cursor","pointer");
        cancelButton.getStyle().set("color","white");
        cancelButton.getStyle().set("margin-top", "25px");
        cancelButton.addClickListener(buttonClickEvent -> {
            d.close();
        });
        content.add(titleLab, descrizione, cancelButton);

        d.add(content);
        d.open();
    }

    public Dialog descrizioneGiocoDialog(Game game){
        Dialog d = new Dialog();
        d.setCloseOnEsc(false);
        d.setCloseOnOutsideClick(false);
        d.setWidth("640px");
        d.setHeight("320px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("height", "100%");

        Label title = new Label("Info su " + game.getNomeGioco());
        title.getStyle().set("font-size", "32px");

        Label descrizione = new Label();
        descrizione.setText(game.getDescrizioneLungaGioco());
        descrizione.getStyle().set("font-size", "16px");

        Button cancelButton = new Button("Close");
        cancelButton.getStyle().set("background-color","#007d99");
        cancelButton.getStyle().set("cursor","pointer");
        cancelButton.getStyle().set("color","white");
        cancelButton.getStyle().set("margin-top", "50px");
        cancelButton.addClickListener(buttonClickEvent -> {
            d.close();
        });
        content.add(title, descrizione, cancelButton);

        d.add(content);
        return d;
    }
}
