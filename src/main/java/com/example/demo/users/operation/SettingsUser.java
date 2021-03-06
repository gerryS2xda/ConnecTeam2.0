
package com.example.demo.users.operation;

import com.example.demo.entity.Account;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.entityRepository.PartitaRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.utility.AppBarUI;
import com.example.demo.utility.InfoEventUtility;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;


@Route("SettingUser")
@HtmlImport("style.html")
@StyleSheet("frontend://stile/style.css")
@StyleSheet("frontend://stile/uploadButton.css")
@PageTitle("ConnecTeam")
public class SettingsUser extends VerticalLayout {

    private PartitaRepository partitaRepository;
    private AccountRepository accountRepository;
    private Account account;
    private Long id;
    private boolean isNavBarVert;


    public SettingsUser() {

        isNavBarVert = false;
        try {
            accountRepository = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
            account = (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
            partitaRepository = (PartitaRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("partitaRepository");
            id = (Long) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("userId");

            getStyle().set("padding", "0px");
            setId("SettingsUser");

            if(accountRepository == null || account == null){
                throw new IllegalArgumentException("AccountRepository e Account sono null");
            }else if(account.getTypeAccount().equals("teacher")){  //Usa la teacherUI se account e' teacher, else usa solo NavBar orrizzontale
                isNavBarVert = true;
            }else{
                NavBar navBar = new NavBar(false);
                add(navBar);
            }

            HorizontalLayout main = new HorizontalLayout();
            main.addClassName("mainHorizontalLayout");
            if(isNavBarVert) {
                getStyle().set("height", "100%"); //per nav bar verticale
                getStyle().set("margin-left", "0px");
                //main.getStyle().set("margin-left", "252px"); //magine sinistro da NavBar verticale
                HorizontalLayout hor = new HorizontalLayout();
                hor.setPadding(false);
                hor.setSpacing(false);
                AppBarUI appBar = new AppBarUI("Settings", false, false); //Nota: true se viene usata una pagina dedicata per SettingsUser
                hor.add(appBar);
                add(hor);
            }

            VerticalLayout personalInfo = new VerticalLayout();
            Label section = new Label("Informazioni personali");
            section.getStyle().set("font-size", "30px");
            VerticalLayout verticalLayout1 = new VerticalLayout();
            Label nome = new Label("Nome: " + account.getNome());
            Label email = new Label("Email: " + account.getEmail());
            verticalLayout1.add(nome, email);
            personalInfo.add(section, verticalLayout1);


            VerticalLayout layoutCambioPassword = new VerticalLayout();
            Label section2 = new Label("Cambia Password");
            section2.getStyle().set("font-size", "30px");
            layoutCambioPassword.add(section2);
            layoutCambioPassword.add(sezioneCambioPassword());


            VerticalLayout layoutElimina = new VerticalLayout();
            Label section4 = new Label("Elimina account");
            section4.getStyle().set("font-size", "30px");
            layoutElimina.add(section4);
            layoutElimina.add(sezioneEliminaAccount());

            VerticalLayout uploadImage = new VerticalLayout();
            Label section5 = new Label("Aggiorna immagine profilo");
            section5.getStyle().set("font-size", "30px");
            section5.getStyle().set("width", "400px");
            uploadImage.add(section5);
            uploadImage.add(initUploaderImage());

            if(isNavBarVert){  //elimina una "colonna" per fare spazio alla navbar verticale
                personalInfo.getStyle().set("width", "50%");
                main.add(personalInfo);
                layoutCambioPassword.getStyle().set("width", "50%");
                main.add(layoutCambioPassword);

                VerticalLayout layoutUploadElimina = new VerticalLayout();
                layoutUploadElimina.setSpacing(false);
                layoutUploadElimina.setPadding(false);
                layoutUploadElimina.add(uploadImage);
                layoutUploadElimina.add(layoutElimina);
                main.add(layoutUploadElimina);
            }else {
                main.add(personalInfo);
                main.add(layoutCambioPassword);
                main.add(layoutElimina);
                main.add(uploadImage);
            }
            add(main);

        } catch (Exception e) {
            removeAll();
            getStyle().set("background-color", "white");
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
        }
    }

    private VerticalLayout sezioneCambioPassword() {

        InfoEventUtility infoEventUtility = new InfoEventUtility();

        VerticalLayout verticalLayout2 = new VerticalLayout();

        PasswordField originalPassword = new PasswordField("Password");
        PasswordField newPassword = new PasswordField("Nuova password");
        PasswordField repeatPassword = new PasswordField("Ripeti password");

        Button submit = new Button("Cambia password");
        submit.getStyle().set("margin-left", "18px");
        submit.setEnabled(false);
        submit.addClickListener(clickEvent -> {
            if (account.getPassword().equals(originalPassword.getValue())) {
                accountRepository.updatePassword(id, newPassword.getValue());
                infoEventUtility.infoEvent("Password cambiata con successo", "100");
            } else {
                infoEventUtility.infoEvent("Password Errata", "165");
            }
        });

        VerticalLayout placeholder = new VerticalLayout();
        Label notMatch = new Label();

        Binder<Account> binder = new Binder();
        binder.setBean(new Account());

        binder.forField(originalPassword)
                .asRequired("Inserisci la password attuale")
                .bind(Account::getPassword, (person, password) -> {
                });

        binder.forField(newPassword)
                .asRequired("Il campo non può essere vuoto")
                .withValidator(new StringLengthValidator(
                        "Almeno 7 caratteri", 7, null))
                .bind(Account::getPassword, Account::setPassword);
        binder.forField(repeatPassword)
                .asRequired("Conferma password")
                .bind(Account::getPassword, (person, password) -> {
                });

        binder.withValidator(Validator.from(account -> {
            if (newPassword.isEmpty() || repeatPassword.isEmpty()) {
                UI.getCurrent().access(() -> placeholder.remove(notMatch));
                return true;
            } else {
                if (Objects.equals(newPassword.getValue(), repeatPassword.getValue())) {
                    UI.getCurrent().access(() -> placeholder.remove(notMatch));
                    return true;
                } else {
                    UI.getCurrent().access(() -> placeholder.add(notMatch));
                    return false;
                }
            }
        }, ""));

        binder.addStatusChangeListener(
                event -> UI.getCurrent().access(() -> {
                    submit.setEnabled(binder.isValid());
                }));


        verticalLayout2.add(originalPassword, newPassword, repeatPassword, submit);
        return verticalLayout2;

    }

    private VerticalLayout sezioneEliminaAccount() {

        VerticalLayout verticalLayout3 = new VerticalLayout();
        Button elimina = new Button("Elimina");
        elimina.getStyle().set("margin-left", "45px");
        elimina.addClickListener(clickEvent -> {
            Dialog window = new Dialog();
            HorizontalLayout layout = new HorizontalLayout();
            Label deleteLabel = new Label("Sicuro di voler eliminare l'account?");
            layout.add(deleteLabel);
            window.open();
            window.add(layout);
            Button delete = new Button("Si");
            delete.addClickListener(buttonClickEvent -> {
                partitaRepository.deleteAccountsPartite(id);
                accountRepository.deleteById(id);
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("loggato", false);
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("accountId", null);
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("account", null);
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEvent("Account eliminato con successo", "100");
                window.close();
            });

            Button no = new Button("No");
            no.addClickListener(buttonClickEvent -> {
                window.close();
            });
            layout.add(delete, no);
        });
        verticalLayout3.add(elimina);

        return verticalLayout3;
    }

    private VerticalLayout initUploaderImage() {

        VerticalLayout verticalLayout4 = new VerticalLayout();

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg","image/jpg", "image/png", "image/gif");
        upload.addClassName("small");
        upload.addSucceededListener(event -> {
            String attachmentName = event.getFileName();
            try {

                // The image can be jpg png or gif, but we store it always as png file in this example
                BufferedImage inputImage = ImageIO.read(buffer.getInputStream(attachmentName));
                ByteArrayOutputStream pngContent = new ByteArrayOutputStream();
                ImageIO.write(inputImage, "png", pngContent);

                account.setProfilePicture(pngContent.toByteArray());
                saveProfilePicture(pngContent.toByteArray());
                InfoEventUtility infoEventUtility = new InfoEventUtility();
                infoEventUtility.infoEvent("Immagine cambiata con successo","100");

            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        verticalLayout4.add(upload);
        return verticalLayout4;
    }

    private void saveProfilePicture(byte[] imageBytes) {
        accountRepository.updateImage(account.getId(), imageBytes);
    }
}

