package com.example.demo.chat;

import com.example.demo.entity.Account;
import com.example.demo.entity.Gruppo;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.gamesRules.Game;
import com.example.demo.guess.gamesMenagemet.frondend.GuessUI;
import com.example.demo.utility.MessageList;
import com.example.demo.utility.Utils;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@StyleSheet("frontend://stile/chat.css")  //da inserire in ogni classe in cui si utilizza 'ChatUI'
public class ChatUI extends VerticalLayout {

    //instance field
    private Account account;
    private Game game;
    private List<Gruppo> gruppi = new ArrayList<Gruppo>();
    private AccountRepository accountRepository;
    private ArrayList<MessageList> spazioMessaggiGruppi;
    private ArrayList<MessageList> spazioMessaggiTeacher;
    private Div chatContainer; //contenitore dello spazio dei messaggi di ogni gruppo
    private Div chatContainerTeacher;
    private TextField message1;
    private boolean isTeacher = false;
    private Image image333; //icona utente nella chat

    //Constructor
    public ChatUI(Game game, Account currentAccount, AccountRepository accountRepository, List<Gruppo> gruppi){

        //Inizializzazione
        this.accountRepository = accountRepository;
        this.account = currentAccount;
        this.gruppi = gruppi;
        this.game = game;
        chatContainer = new Div();
        chatContainer.addClassName("chat");
        chatContainerTeacher = new Div();
        chatContainerTeacher.addClassName("chat");
        spazioMessaggiGruppi = new ArrayList<MessageList>();
        spazioMessaggiTeacher = new ArrayList<MessageList>();

        if(account.getTypeAccount().equals("teacher"))
            isTeacher = true;

        //Init mainContent
        setSpacing(false);
        setPadding(false);
        addClassName("mainContentVertLayout");

        Label label = new Label("Chat");
        label.addClassName("chatLabelTitle");
        add(label);

        if(isTeacher){
            add(chatContainerTeacher);
        }else{
            add(chatContainer);
        }

        HorizontalLayout textFieldSendBtn = new HorizontalLayout();
        textFieldSendBtn.getElement().setAttribute("id", "containerTFBtnSendchat");
        textFieldSendBtn.setSpacing(false);
        textFieldSendBtn.addClassName("textFieldSendBtnHorLayout");

        message1 = new TextField();
        message1.addClassName("textFieldEnterMessage");
        message1.setPlaceholder("Enter message...");
        textFieldSendBtn.add(message1, sendButtonConfiguration());

        for(Gruppo x : gruppi){
            MessageList spazioMessaggiChat = new MessageList("chatlayoutmessage2");
            spazioMessaggiChat.getElement().setAttribute("name", x.getId());
            spazioMessaggiChat.getStyle().set("display", "none");
            spazioMessaggiGruppi.add(spazioMessaggiChat);
            chatContainer.add(spazioMessaggiChat);
            if(Utils.isAccountInThisGruppo(x, account)) {
                spazioMessaggiChat.getStyle().set("display", "block");
            }

            MessageList spazioMsgTeacher = new MessageList("chatlayoutmessage2");
            spazioMsgTeacher.getElement().setAttribute("name", x.getId());
            spazioMsgTeacher.getStyle().set("display", "none");
            spazioMessaggiTeacher.add(spazioMsgTeacher);
            chatContainerTeacher.add(spazioMsgTeacher);
            if(isTeacher){
                String currentGroupSelect = "";
                if(game.getNomeGioco().equals("Guess")){
                    currentGroupSelect =  GuessUI.currentGroupSelect.getId();
                }else if(game.getNomeGioco().equals("Maty")){
                    //Da inserire
                }
                if(x.getId().equals(currentGroupSelect)){
                    spazioMsgTeacher.getStyle().set("display", "block");
                }
            }
        }
        add(textFieldSendBtn);


    }

    private Button sendButtonConfiguration(){
        Icon icon = new Icon(VaadinIcon.PAPERPLANE_O);
        icon.setSize("24px");
        icon.setColor("white");
        icon.getStyle().set("left", "100px");
        Button send = new Button(icon);
        message1.addKeyDownListener(Key.ENTER, keyDownEvent -> {
            String mess = message1.getValue();
            if (!mess.equals("")) {
                if(account.getTypeAccount().equals("teacher")) {
                    String currentGroupSelect = "";
                    if(game.getNomeGioco().equals("Guess")){
                        currentGroupSelect =  GuessUI.currentGroupSelect.getId();
                    }else if(game.getNomeGioco().equals("Maty")){
                        //Da inserire
                    }
                    BroadcasterChat.broadcast(Utils.findGruppoByName(gruppi, currentGroupSelect), "Teacher:" + message1.getValue() + ":" + account.getId());
                }else{
                    BroadcasterChat.broadcast(Utils.findGruppoByAccount(gruppi, account), account.getNome() + ": " + message1.getValue()+":"+account.getId());
                }
                message1.setValue("");
            }
        });

        send.addClickListener(buttonClickEvent -> {
            String mess = message1.getValue();
            if (!mess.equals("")) {
                if(isTeacher) {
                    String currentGroupSelect = "";
                    if(game.getNomeGioco().equals("Guess")){
                        currentGroupSelect =  GuessUI.currentGroupSelect.getId();
                    }else if(game.getNomeGioco().equals("Maty")){
                        //Da inserire
                    }
                    BroadcasterChat.broadcast(Utils.findGruppoByName(gruppi, currentGroupSelect), "Teacher:" + message1.getValue() + ":" + account.getId());
                }else{
                    BroadcasterChat.broadcast(Utils.findGruppoByAccount(gruppi, account), account.getNome() + ": " + message1.getValue()+":"+account.getId());
                }
                message1.setValue("");
            }
        });
        send.addClassName("buttonSendChat");
        return send;
    }

    private Image generateImage(Account account) {
        Long id = account.getId();
        StreamResource sr = new StreamResource("user", () ->  {
            Account attached = accountRepository.findWithPropertyPictureAttachedById(id);
            return new ByteArrayInputStream(attached.getProfilePicture());
        });
        sr.setContentType("image/png");
        Image image = new Image(sr, "profile-picture");
        return image;
    }


    //public methods
    public void receiveBroadcast(Gruppo g, String message) { //g e' il gruppo da cui e' stato inviato il messaggio
        MessageList spazioMsg = Utils.getMessageListFromListByAttributeForChat(spazioMessaggiGruppi, "name", g.getId());
        MessageList spazioMsgTeacher = Utils.getMessageListFromListByAttributeForChat(spazioMessaggiTeacher, "name", g.getId());


        String string = message;
            String[] parts = string.split(":");
            String nome = parts[0];
            String testo = parts[1];
            String id = parts[2];
            Account a = accountRepository.findAccountById(Long.parseLong(id));

            VerticalLayout messageContainer = new VerticalLayout();
            messageContainer.setSpacing(false);
            messageContainer.setPadding(false);
            messageContainer.setWidth("100%");

            HorizontalLayout container = new HorizontalLayout();
            container.setWidth("100%");
            container.addClassName("message");

            VerticalLayout nomePMsgContainer = new VerticalLayout();
            nomePMsgContainer.setPadding(false);
            nomePMsgContainer.setSpacing(false);
            nomePMsgContainer.setWidth("auto");

            Label nomeU = new Label(nome);
            Paragraph msgContenuto = new Paragraph(testo);
            msgContenuto.addClassName("paragraphMsgContent");
            nomePMsgContainer.add(nomeU, msgContenuto);

            if(nome.equals("Teacher") && isTeacher){
                //icona dell'utente che ha inviato il messaggio
                if (a.getProfilePicture()!=null){
                    image333 = generateImage(a);
                    image333.getStyle().set("order","0");
                    container.add(image333);
                }else {
                    if(a.getSesso()=="1"){
                        image333 = new Image("frontend/img/profiloGirl.png", "foto profilo");
                        image333.getStyle().set("order","0");
                        container.add(image333);
                    }
                    else {
                        image333 = new Image("frontend/img/profiloBoy.png", "foto profilo");
                        image333.getStyle().set("order","0");
                        container.add(image333);
                    }
                }

                container.getStyle().set("padding-right", "36px");
                nomeU.addClassName("nomeUserLabelLeft");
                nomePMsgContainer.addClassName("nomePMsgContainerVertLayout");
                container.add(nomePMsgContainer);

                messageContainer.add(container);
                spazioMsgTeacher.add(messageContainer);
                return;
            }

            if(!nome.equals(account.getNome())){  //se l'utente che ha inviato il msg non e' 'account' (cioe' NON sono io)
                messageContainer.getStyle().set("align-items", "flex-end");
                container.setWidth("auto");
                container.getStyle().set("padding-left", "24px");
                container.getStyle().set("padding-right", "24px");
                container.getStyle().set("margin", "10px 0 0 0");
                nomeU.addClassName("nomeUserLabelRight");
                nomePMsgContainer.addClassName("nomePMsgContainerVertLayoutRight");
                container.add(nomePMsgContainer);
            }

            //icona dell'utente che ha inviato il messaggio
            if (a.getProfilePicture()!=null){
                image333 = generateImage(a);
                image333.getStyle().set("order","0");
                container.add(image333);
            }else {
                if(a.getSesso()=="1"){
                    image333 = new Image("frontend/img/profiloGirl.png", "foto profilo");
                    image333.getStyle().set("order","0");
                    container.add(image333);
                }
                else {
                    image333 = new Image("frontend/img/profiloBoy.png", "foto profilo");
                    image333.getStyle().set("order","0");
                    container.add(image333);
                }
            }

            if(nome.equals(account.getNome())) {  //se l'utente che ha inviato il msg e' 'account' (cioe' sono io))
                container.getStyle().set("padding-right", "36px");
                nomeU.addClassName("nomeUserLabelLeft");
                nomePMsgContainer.addClassName("nomePMsgContainerVertLayout");
                container.add(nomePMsgContainer);
            }else{
                image333.getStyle().set("margin", "0px 0px 0px 10px");
            }

            messageContainer.add(container);

            if(isTeacher){
                spazioMsgTeacher.add(messageContainer);
                return;
            }

            //Solo ai membri del gruppo di cui fa parte 'account' viene mostrato il messaggio
            if(Utils.isAccountInThisGruppo(g, account)){
                spazioMsg.add(messageContainer);
                spazioMsg.getStyle().set("display", "block");
            }else{ //per gli account che non fanno parte del gruppo, containerPV rimane invisibile
                spazioMsg.getStyle().set("display", "none");
            }
    }

    public void hideAllSpazioMessaggiTeacher(){
        for(MessageList i : spazioMessaggiTeacher){
            i.getStyle().set("display", "none");
        }
    }

    //Getter and setter
    public ArrayList<MessageList> getSpazioMessaggiGruppi() {
        return spazioMessaggiGruppi;
    }

    public ArrayList<MessageList> getSpazioMessaggiTeacher() {
        return spazioMessaggiTeacher;
    }

    public Div getChatContainer() {
        return chatContainer;
    }

    public Div getChatContainerTeacher() {
        return chatContainerTeacher;
    }
}
