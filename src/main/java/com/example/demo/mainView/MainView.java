package com.example.demo.mainView;



import com.example.demo.entity.Account;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.gamesRules.GameList;
import com.example.demo.guess.gamesMenagemet.backend.db.ItemRepository;
import com.example.demo.maty.gameMenagement.backend.db.ItemRepositoryMaty;
import com.example.demo.nuovoGioco.gameManagement.database.ItemRepositoryNuovoGioco;
import com.example.demo.users.controller.ControllerMainUI;   //modificato
import com.example.demo.users.controller.TeacherMainUITab;
import com.example.demo.users.discusser.StudentHomeView;
import com.example.demo.utility.DialogUtility;
import com.example.demo.utility.InfoEventUtility;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;


@PWA(name = "Connecteam", shortName = "Connecteam",
        offlinePath = "offline-page.html",
        iconPath = "icon.png")
@Route("")
@HtmlImport("style.html")
@StyleSheet("frontend://stile/style.css")
@PageTitle("ConnecTeam")
public class MainView extends VerticalLayout {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PartitaRepository partitaRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRepositoryMaty itemRepositoryMaty;

    @Autowired
    private ItemRepositoryNuovoGioco itemRepositoryNuovoGioco;

    @Autowired
    private GameList gameList;

    //instance field
    private TextField email;
    private String confermaReg;
    private Button b;
    private Image logoImg;
    private Image pcImg;
    private Div descrizione; //contiene Label 'description'
    private VerticalLayout loginContainer;
    private Div divContainerFormLogin;
    private VerticalLayout registerContainer;
    private Div divContainerFormRegister;
    private HorizontalLayout btnContainer = new HorizontalLayout();
    private boolean isBtnRegisterLoginShow = false;

    public MainView() {

        try {
            /*
            confermaReg = VaadinRequest.getCurrent().getParameter("confermaRegistrazione");
            if(confermaReg!=null){
                completaRegistrazione(confermaReg);
            }
            else {
            */
                loadMainViewContent();

                //Using Browser Window Resize Events for responsive
                UI.getCurrent().getPage().addBrowserWindowResizeListener(browserWindowResizeEvent -> {
                    loadResponsiveConfiguration(browserWindowResizeEvent.getWidth(), browserWindowResizeEvent.getHeight());
                });

        }catch (Exception e){
            removeAll();
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
            e.printStackTrace();
        }
    }

    private void loadMainViewContent(){

        isBtnRegisterLoginShow = false;

        setSizeFull();
        getElement().getStyle().set("background-color", "#b8dbe0");

        logoImg = logoConnectTeam();
        pcImg = pcImage();

        loginContainer = login(false); //mostra il separatore tra login container e resto della pagina
        add(loginContainer);

        registerContainer = register(false);
        add(registerContainer);

        descrizione = descrizioneContainer();
        add(descrizione, logoImg, pcImg);

        //Button che mostra un lista di utenti gia' registrati (Testing)
        Button button = new Button();
        button.addClickListener(buttonClickEvent -> {
            buttons();
        });
        add(button);
    }

    private Image logoConnectTeam(){
        Image img = new Image("frontend/img/logocompleto.png", "txt");
        img.setWidth("400px");
        img.setHeight("120px");
        img.getStyle().set("top","5px");
        img.getElement().getStyle().set("position", "absolute");
        return img;
    }

    private Image pcImage(){
        Image img = new Image("frontend/img/pc.png", "txt");
        img.getElement().getStyle().set("position", "absolute");
        img.getElement().getStyle().set("width", "43%");
        img.getElement().getStyle().set("height", "55%");
        img.getElement().getStyle().set("top", "230px"); //valore precedente: 35%
        img.getElement().getStyle().set("opacity", "0.5");
        img.getElement().getStyle().set("left", "200px");
        setAlignSelf(Alignment.START, img);
        return img;
    }

    private Div descrizioneContainer(){
        Div container = new Div();
        Label descr = new Label("\n" +
                "ConnecTeam è una piattaforma web che ha come obiettivo l'apprendimento collaborativo tramite dei giochi." +
                " I giochi possono essere per qualsiasi fascia d'età e di solito sono prettamente educativi." +
                " La collaborazione è fondamentale per ConnecTeam!" +
                " Inoltre non mancano i servizi per l'utente, il quale troverà molto semplice gestire il suo profilo!");
        descr.setWidth("100%");
        container.setWidth("61%"); //valore precedente: 64%
        container.setHeight("10%");
        container.getElement().getStyle().set("position", "absolute");
        container.getElement().getStyle().set("top", "150px");
        container.add(descr);
        return container;
    }

    private VerticalLayout login(boolean isInBtn){

        VerticalLayout divFormLogin = new VerticalLayout();

        //content
        TextField emailField = new TextField("Email");
        PasswordField passwordField = new PasswordField("Password");
        passwordField.getElement().getStyle().set("margin-left","10px");
        passwordField.getElement().getStyle().set("margin-right","10px");
        Button login = new Button("Accedi");
        login.setEnabled(false);
        Binder<Account> binder = new Binder<>();
        binder.forField(emailField)
                .asRequired("Inserisci l'E-mail")
                .withValidator(new EmailValidator("Indirizzo E-mail non valido"))
                .bind(Account::getEmail, Account::setEmail);
        binder.forField(passwordField)
                .asRequired("Inserisci la Password")
                .bind(Account::getPassword, Account::setPassword);
        binder.addStatusChangeListener(
                event -> login.setEnabled(binder.isValid()));

        login.addClickShortcut(Key.ENTER);

        login.addClickListener(buttonClickEvent -> {
            Account a = accountRepository.findOneByEmail(emailField.getValue());
            if ( a != null && a.getPassword().equals(passwordField.getValue())) {
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loggato", true);
                VaadinService.getCurrentRequest().getWrappedSession().
                        setAttribute("userId", accountRepository.findOneByEmail(emailField.getValue()).getId());
                VaadinService.getCurrentRequest().getWrappedSession().
                        setAttribute("user", accountRepository.findOneByEmail(emailField.getValue()));
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("rep",accountRepository);
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("partitaRepository",partitaRepository);
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("gameList",gameList);
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepository",itemRepository);
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepositoryMaty",itemRepositoryMaty);
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepositoryNuovoGioco",itemRepositoryNuovoGioco);

                if(a.getTypeAccount().equals("teacher")){
                    UI.getCurrent().navigate(ControllerMainUI.class);
                }else{
                    UI.getCurrent().navigate(StudentHomeView.class);
                }
            }else {
                InfoEventUtility infoEventUtility =  new InfoEventUtility();
                infoEventUtility.infoEvent("E-mail e/o password errati","100");
            }
        });

        RouterLink passwordDimenticata = new RouterLink();
        passwordDimenticata.setText("Password dimenticata?");
        passwordDimenticata.add(new Button("Clicca qui",buttonClickEvent -> {
            VaadinRequest.getCurrent().getWrappedSession().setAttribute("rep",accountRepository);
            DialogUtility dialogUtility = new DialogUtility();
            dialogUtility.passwordDimenticata();
        }));

        Div space = new Div();
        space.setWidth("100%");

        divContainerFormLogin = new Div();
        divContainerFormLogin.add(emailField,passwordField,login,space,passwordDimenticata);
        divFormLogin.setHorizontalComponentAlignment(Alignment.END,divContainerFormLogin);

        if(isInBtn) {
            divFormLogin.add(divContainerFormLogin);
        }else{
            Span s = new Span();
            s.getStyle().set("background-color", "#007d99");
            s.getStyle().set("flex", "0 0 2px");
            s.getStyle().set("align-self", "center");
            s.getStyle().set("width", "90%");
            s.getStyle().set("margin-top", "10px");
            divFormLogin.add(divContainerFormLogin,s);
        }

        return divFormLogin;
    }

    private VerticalLayout register(boolean isInBtn){

        VerticalLayout divForm = new VerticalLayout();

        //Content
        Label label = new Label("Crea un nuovo account");
        label.getElement().getStyle().set("font-size","30px");

        TextField nome = new TextField("Nome");
        email = new TextField("Email");
        email.getElement().getStyle().set("margin-left","10px");
        PasswordField password = new PasswordField("Password");
        PasswordField passwordC = new PasswordField("Conferma Password");
        passwordC.getElement().getStyle().set("margin-left","10px");
        Button submit = new Button("Registrati");
        submit.getElement().getStyle().set("margin-left","25px");
        submit.getElement().getStyle().set("margin-top","25px");
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setLabel("Sesso");
        group.setItems("Uomo", "Donna");
        Binder<Account> binder = new Binder<>();

        binder.forField(nome)
                .asRequired("Inserisci il Nome")
                .bind(Account::getNome, Account::setNome);

        binder.forField(email)
                .asRequired("Inserisci l'E-mail")
                .withValidator(new EmailValidator("Indirizzo e-mail non valido"))
                .bind(Account::getEmail, Account::setEmail);

        binder.forField(password)
                .asRequired("Inserisci Password")
                .withValidator(new StringLengthValidator(
                        "Almeno 7 caretteri", 7, null))
                .bind(Account::getPassword, Account::setPassword);

        binder.forField(passwordC)
                .asRequired("Conferma Password")
                .withValidator(Validator.from(account -> {
                    if (password.isEmpty() || passwordC.isEmpty()) {
                        return true;
                    } else {
                        return Objects.equals(password.getValue(),
                                passwordC.getValue());
                    }
                }, "Le password non coincidono"))
                .bind(Account::getPassword, (person, password1) -> {});

        binder.forField(group)
                .asRequired("Sesso")
                .bind(Account::getSesso, Account::setSesso);

        Label validationStatus = new Label();
        binder.setStatusLabel(validationStatus);
        binder.setBean(new Account());
        submit.setEnabled(false);

        binder.addStatusChangeListener(
                event -> submit.setEnabled(binder.isValid()));

        divContainerFormRegister = new Div();

        submit.addClickListener(buttonClickEvent -> {
            if (group.getValue().equals("Uomo")){
                binder.getBean().setSesso("0");
            }else {
                binder.getBean().setSesso("1");
            }
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("rep",accountRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("partitaRepository",partitaRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("gameList",gameList);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepository",itemRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepositoryMaty",itemRepositoryMaty);
            registraNewAccount(binder.getBean());
        });

        if(isInBtn){
            divContainerFormRegister.getElement().getStyle().set("margin-right","0");
            divContainerFormRegister.getElement().getStyle().set("position","relative");
            divContainerFormRegister.getElement().getStyle().set("top","0");
        }else{
            divContainerFormRegister.getElement().getStyle().set("margin-right","100px");
            divContainerFormRegister.getElement().getStyle().set("position","relative");
            divContainerFormRegister.getElement().getStyle().set("top","-20px");
        }


        Div space = new Div();
        space.setWidth("100%");
        Div space2 = new Div();
        space2.setWidth("100%");
        Div space3 = new Div();
        space3.setWidth("100%");

        divContainerFormRegister.add(label,space3,nome,email,space,password,passwordC,space2,group,submit);
        divForm.setHorizontalComponentAlignment(Alignment.END,divContainerFormRegister);

        divForm.add(divContainerFormRegister);
        return divForm;
    }

    private void registraNewAccount(Account account){
        AccountRepository a = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
        if (a.findOneByEmail(account.getEmail()) == null) { //se true -> gestisci nuova email
            if(account.getEmail().contains("@unisa.it")){
                account.setTypeAccount("teacher");
            }else{
                account.setTypeAccount("student");
            }
            completaRegistrazione(account);
        }else {
            InfoEventUtility infoEventUtility = new InfoEventUtility();
            infoEventUtility.infoEvent("E-mail esistente, riprova","100");
        }
    }

    private void completaRegistrazione(Account account){

        AccountRepository a = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
        try{
            a.save(account);  //salva il nuovo account nel DB
        }catch (Exception e){
            InfoEventUtility infoEventUtility = new InfoEventUtility();
            infoEventUtility.infoEvent("Impossibile completare la registrazione","100");
            return;
        }
        DialogUtility dialogUtility = new DialogUtility();
        dialogUtility.loginDialog(account);
    }

    private void loadResponsiveConfiguration(int widthBrowser, int heightBrowser){

        if(widthBrowser < 1000 && heightBrowser <= 500){
            pcImg.getStyle().set("display", "none");
            descrizione.getStyle().set("margin-left", "20px");
            descrizione.getStyle().set("width", "35%");
            descrizione.getStyle().set("top", "180px");
            divContainerFormRegister.getStyle().set("margin-right", "0");
            divContainerFormRegister.getStyle().set("position", "relative");
            divContainerFormRegister.getStyle().set("right", "10%");
        }else{
            removeAll();
            loadMainViewContent();
            return;
        }

        if(widthBrowser <= 850 && heightBrowser <= 500){
            logoImg.setWidth("40%");
            logoImg.setHeight("25%");
            divContainerFormRegister.getStyle().set("right", "0");
        }else {
            divContainerFormRegister.getStyle().set("right", "10%");
        }

        if(widthBrowser <= 770 && heightBrowser <= 500){
            descrizione.getStyle().set("width", "85%");
            if(!isBtnRegisterLoginShow) {
               divContainerFormLogin.getStyle().set("display", "none");
               registerContainer.getStyle().set("display", "none");
               loginContainer.getStyle().set("position", "relative");
               loginContainer.getStyle().set("top", "120px");
               btnContainer = buttonLoginRegisterContainer();
               add(btnContainer);
               isBtnRegisterLoginShow = true;
           }
        }else{
            descrizione.getStyle().set("width", "35%");
            divContainerFormLogin.getStyle().set("display", "block");
            registerContainer.getStyle().set("display", "flex"); //VerticalLayout richiede display:flex
            loginContainer.getStyle().set("position", "relative");
            loginContainer.getStyle().set("top", "0");
            isBtnRegisterLoginShow = false;
            remove(btnContainer);
        }
    }

    private HorizontalLayout buttonLoginRegisterContainer(){
        HorizontalLayout container = new HorizontalLayout();
        container.getStyle().set("position", "absolute");
        container.getStyle().set("left", "50%");

        Button login = new Button("Login");
        login.addClickListener(buttonClickEvent -> {
           Dialog d = new Dialog();
           d.setWidth("100%");
           d.setHeight("100%");
           d.add(login(true));
           d.open();
        });

        Button register = new Button("Registrazione");
        register.addClickListener(buttonClickEvent -> {
            Dialog d = new Dialog();
            d.setWidth("100%");
            d.setHeight("100%");
            d.add(register(true));
            d.open();
        });

        container.add(login, register);
        return container;
    }

    //Test buttons per accedere direttamente al sito con account gia' registrati
    private void buttons(){

        b = new Button("gregorio");
        add(b);
        b.addClickListener(buttonClickEvent -> {

            Account a = accountRepository.findOneByEmail("gregorio@gmail.com");
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loggato", true);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("userId", a.getId());
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("user", a);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("rep",accountRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("partitaRepository",partitaRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("gameList",gameList);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepository",itemRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepositoryMaty",itemRepositoryMaty);

            UI.getCurrent().navigate(TeacherMainUITab.class);
        });

        b = new Button("luigi");
        add(b);
        b.addClickListener(buttonClickEvent -> {
            Account a = accountRepository.findOneByEmail("luigi@gmail.com");
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loggato", true);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("userId", a.getId());
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("user", a);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("rep",accountRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("partitaRepository",partitaRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("gameList",gameList);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepository",itemRepository);

            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepositoryMaty",itemRepositoryMaty);

            UI.getCurrent().navigate(TeacherMainUITab.class);
        });

        b = new Button("michela");
        add(b);
        b.addClickListener(buttonClickEvent -> {
            Account a = accountRepository.findOneByEmail("michela@gmail.com");
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loggato", true);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("userId", a.getId());
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("user", a);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("rep",accountRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("partitaRepository",partitaRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("gameList",gameList);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepository",itemRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepositoryMaty",itemRepositoryMaty);

            UI.getCurrent().navigate(StudentHomeView.class);
        });

        b = new Button("francesca");
        add(b);
        b.addClickListener(buttonClickEvent -> {
            Account a = accountRepository.findOneByEmail("francesca@gmail.com");
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loggato", true);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("userId", a.getId());
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("user", a);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("rep",accountRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("partitaRepository",partitaRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("gameList",gameList);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepository",itemRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepositoryMaty",itemRepositoryMaty);

            UI.getCurrent().navigate(StudentHomeView.class);
        });

        b = new Button("antonio");
        add(b);
        b.addClickListener(buttonClickEvent -> {
            Account a = accountRepository.findOneByEmail("antonio@gmail.com");
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loggato", true);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("userId", a.getId());
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("user", a);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("rep",accountRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("partitaRepository",partitaRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("gameList",gameList);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepository",itemRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepositoryMaty",itemRepositoryMaty);

            UI.getCurrent().navigate(StudentHomeView.class);
        });

        b = new Button("gianluca");
        add(b);
        b.addClickListener(buttonClickEvent -> {
            Account a = accountRepository.findOneByEmail("gianluca@gmail.com");
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loggato", true);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("userId", a.getId());
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("user", a);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("rep",accountRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("partitaRepository",partitaRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("gameList",gameList);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepository",itemRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepositoryMaty",itemRepositoryMaty);

            UI.getCurrent().navigate(StudentHomeView.class);
        });

        b = new Button("simone");
        add(b);
        b.addClickListener(buttonClickEvent -> {
            Account a = accountRepository.findOneByEmail("simone@gmail.com");
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loggato", true);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("userId", a.getId());
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("user", a);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("rep",accountRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("partitaRepository",partitaRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("gameList",gameList);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepository",itemRepository);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("itemRepositoryMaty",itemRepositoryMaty);

            UI.getCurrent().navigate(StudentHomeView.class);
        });

    }

    /* Codice per registrare nuovi account usando Gmail per verificare esistenza della mail inserita
    private void registraNewAccount(Account account){

        AccountRepository a = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
        if (a.findOneByEmail(account.getEmail()) == null) {
            Random random = new Random();
            int n = random.nextInt(9000) + 1000;
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("codiceRegistrazione", "" + n);
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("accountDaRegistrare", account);
            SendMail.sendMailTLS(email.getValue(), "ConnecTeam-Conferma E-mail",
                    "clicca sul link per completare la registrazione " +
                            "http://localhost:8080/?confermaRegistrazione=" + n);
            InfoEventUtility infoEventUtility = new InfoEventUtility();
            infoEventUtility.infoEvent("Ti abbiamo inviato un'e-mail all'indirizzo: "+ email.getValue(),"0");
        }else {
            InfoEventUtility infoEventUtility = new InfoEventUtility();
            infoEventUtility.infoEvent("E-mail esistente, riprova","100");
        }
    }

    private void completaRegistrazione(String conferma){

        String confermaAttribute = (String) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("codiceRegistrazione");
        if(confermaAttribute==null || !confermaAttribute.equals(conferma)){
            InfoEventUtility infoEventUtility = new InfoEventUtility();
            infoEventUtility.infoEvent("Impossibile completare la registrazione","100");
            return;
        }
        VaadinService.getCurrentRequest().getWrappedSession().setAttribute("codiceRegistrazione", null);
        Account account= (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("accountDaRegistrare");
        if(account==null){
            InfoEventUtility infoEventUtility = new InfoEventUtility();
            infoEventUtility.infoEvent("Account non valido","100");
            return;
        }
        AccountRepository a = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
        try{
            a.save(account);
        }catch (Exception e){
            InfoEventUtility infoEventUtility = new InfoEventUtility();
            infoEventUtility.infoEvent("Account non valido","100");
            return;
        }
        VaadinService.getCurrentRequest().getWrappedSession().setAttribute("accountDaRegistrare", null);
        DialogUtility dialogUtility = new DialogUtility();
        dialogUtility.loginDialog(account);
    }

    */

}

