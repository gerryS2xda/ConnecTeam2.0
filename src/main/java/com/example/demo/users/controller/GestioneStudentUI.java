package com.example.demo.users.controller;

import com.example.demo.users.broadcaster.BroadcastListener;
import com.example.demo.users.broadcaster.Broadcaster;
import com.example.demo.users.event.AccountListEventBeanListener;
import com.example.demo.entity.Account;
import com.example.demo.entityRepository.AccountRepository;
import com.example.demo.error.ErrorPage;
import com.example.demo.userOperation.NavBarVertical;
import com.example.demo.users.event.StartGameEventBeanPublisher;
import com.example.demo.utility.AppBarUI;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.*;

@Push
@Route("ControllerGestStud")
@HtmlImport("style.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/gestStudStyle.css")
@PageTitle("Gestione Studenti")
public class GestioneStudentUI extends HorizontalLayout implements BroadcastListener {

    //instance field
    private AccountRepository accRep;
    private Account account;
    private Image imageU = new Image(); //immagine dell'utente
    private Grid<Account> gridStud = new Grid<>(Account.class);
    private Grid<Account> gridGuess = new Grid<>(Account.class);
    private Grid<Account> gridMaty = new Grid<>(Account.class);
    private Grid<Account> gridNewGame = new Grid<>(Account.class);
    private List<Account> draggedItems; //elementi soggetti a DnD
    private Grid<Account> dragSource; //sorgente da cui 'parte' il DnD
    private AccountListEventBeanListener accountListEventBeanListener;
    private StartGameEventBeanPublisher startGameEventBeanPublisher;
    //NOTA: uso questa variabile per tenere traccia della lista account corrente
    //BUG: "Premi 'Aggiorna' viene caricata una nuova lista e si perdono le modifiche attuali (fare merge tra due liste)
    private Map<Account, String> currentAccountList = new HashMap<>(); //lista corrente di account list

    public GestioneStudentUI(@Autowired AccountListEventBeanListener accountEventListener, @Autowired StartGameEventBeanPublisher startGameEventPublisher){
        try {
            accRep = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
            account = (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
            accountListEventBeanListener = accountEventListener; //ottenere event su lista studenti collegati
            startGameEventBeanPublisher = startGameEventPublisher;

            setId("GestioneStudentUI"); //setta id del root element di questo component

            //Registra un teacher listener
            Broadcaster.registerTeacher(account, this);

            //Nav bar verticale e appBar
            getStyle().set("height", "100%"); //per nav bar verticale
            getStyle().set("width", "100%");
            NavBarVertical navBar = new NavBarVertical();
            add(navBar);
            AppBarUI appBar = new AppBarUI("Gestione studenti", false); //nome pagina corrente
            add(appBar);


            Paragraph guidetxt = new Paragraph("Selezionate uno studente dalla lista e trascinatelo nel gioco che desiderate." +
                    "Premere il pulsante 'Gioca' per iniziare una nuova partita con il gruppo di studenti che è stato impostato.");
            guidetxt.addClassName("guideText");
            guidetxt.getStyle().set("left", NavBarVertical.NAVBAR_WIDTH);


            //updateAndMergeAccountList();
            //gridStud.setItems(currentAccountList.keySet()); //keyset() poiche' gli account rappresentano le chiavi
            configurationGridDragAndDrop();

            VerticalLayout contStud = containerListStudent("250px", "70%");
            //VerticalLayout contMoreGame = createExpandablePanelGames("250px", "70%");
            VerticalLayout contGuess = containerListStudentGuess("250px", "70%");
            VerticalLayout contMaty = containerListStudentMaty("250px", "70%");
            VerticalLayout contNewGame = containerListStudentNewGame("250px", "70%");

            add(guidetxt, contStud, contGuess, contMaty, contNewGame);

            UI.getCurrent().setPollInterval(5000);
        }catch (Exception e){
            removeAll();
            getStyle().set("background-color","white");
            ErrorPage errorPage = new ErrorPage();
            add(errorPage);
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }

        private void configurationGridDragAndDrop(){

        gridStud.removeAllColumns(); //inserito per rimuovere tutte le colonne di Account (bug)
        gridStud.setId("AccountList");
        gridStud.addColumn(Account::getNome).setHeader("Studenti collegati");

        gridGuess.removeAllColumns();
        gridGuess.setId("Guess");
        gridGuess.addColumn(Account::getNome).setHeader("Guess");

        gridMaty.removeAllColumns();
        gridMaty.setId("Maty");
        gridMaty.addColumn(Account::getNome).setHeader("Maty");

        gridNewGame.removeAllColumns();
        gridNewGame.setId("NewGame");
        gridNewGame.addColumn(Account::getNome).setHeader("Nuovo gioco");

        ComponentEventListener<GridDragStartEvent<Account>> dragStartListener = event -> {
            draggedItems = event.getDraggedItems();
            System.out.println("TestStartDrag");
            dragSource = event.getSource();
            gridStud.setDropMode(GridDropMode.BETWEEN); //imposta modalita' di trascinamento delle righe
            gridGuess.setDropMode(GridDropMode.BETWEEN); //GridDropMode.BETWEEN: il drop event si verifica tra righe della Grid
            gridMaty.setDropMode(GridDropMode.BETWEEN);
            gridNewGame.setDropMode(GridDropMode.BETWEEN);
        };

        ComponentEventListener<GridDragEndEvent<Account>> dragEndListener = event -> {
            System.out.println("TestEndDrag");
            draggedItems = null;
            dragSource = null;
            gridStud.setDropMode(null);
            gridGuess.setDropMode(null);
            gridMaty.setDropMode(null);
            gridNewGame.setDropMode(null);
        };

        ComponentEventListener<GridDropEvent<Account>> dropListener = event -> {
            Optional<Account> target = event.getDropTargetItem();
            if (target.isPresent() && draggedItems.contains(target.get())) {
                return;
            }

            // Remove the items from the source grid
            @SuppressWarnings("unchecked")
            ListDataProvider<Account> sourceDataProvider = (ListDataProvider<Account>) dragSource.getDataProvider();
            List<Account> sourceItems = new ArrayList<>(sourceDataProvider.getItems());
            sourceItems.removeAll(draggedItems);
            dragSource.setItems(sourceItems);

            // Add dragged items to the target Grid
            Grid<Account> targetGrid = event.getSource();
            @SuppressWarnings("unchecked")
            ListDataProvider<Account> targetDataProvider = (ListDataProvider<Account>) targetGrid.getDataProvider();
            List<Account> targetItems = new ArrayList<>(targetDataProvider.getItems());

            int index = target.map(nome -> targetItems.indexOf(nome)
                    + (event.getDropLocation() == GridDropLocation.BELOW ? 1: 0)).orElse(0);
            targetItems.addAll(index, draggedItems);
            targetGrid.setItems(targetItems);
            System.out.println(draggedItems.toString() + "\n targetGridname: " + targetGrid.getId().get());
            updateAccountListHashMap(draggedItems.get(0), targetGrid.getId().get());
        };

        gridStud.setSelectionMode(Grid.SelectionMode.NONE); //non mostra checkbox per selezionare la riga
        gridStud.addDropListener(dropListener);
        gridStud.addDragStartListener(dragStartListener);
        gridStud.addDragEndListener(dragEndListener);
        gridStud.setRowsDraggable(true); //utente può usare Drag-and-Drop delle righe

        gridGuess.setSelectionMode(Grid.SelectionMode.NONE);
        gridGuess.addDropListener(dropListener);
        gridGuess.addDragStartListener(dragStartListener);
        gridGuess.addDragEndListener(dragEndListener);
        gridGuess.setRowsDraggable(true);

        gridMaty.setSelectionMode(Grid.SelectionMode.NONE);
        gridMaty.addDropListener(dropListener);
        gridMaty.addDragStartListener(dragStartListener);
        gridMaty.addDragEndListener(dragEndListener);
        gridMaty.setRowsDraggable(true);

        gridNewGame.setSelectionMode(Grid.SelectionMode.NONE);
        gridNewGame.addDropListener(dropListener);
        gridNewGame.addDragStartListener(dragStartListener);
        gridNewGame.addDragEndListener(dragEndListener);
        gridNewGame.setRowsDraggable(true);

    }

    private VerticalLayout containerListStudent(String width, String height){
        VerticalLayout vert = new VerticalLayout();
        vert.addClassName("gridContainerVertLayout");
        vert.setSpacing(false);

        gridStud.getStyle().set("width", "100%");
        gridStud.getStyle().set("height", "80%");

        Button btn = new Button("Aggiorna");
        btn.addClassName("btnUnderGrid");
        btn.addClickListener(event ->{
            updateAndMergeAccountList();
            //Rimuovi account che hanno il campo "Game" gia' settato
            Map<Account, String> tempList = new HashMap<>();
            for(Account i : currentAccountList.keySet()){
                tempList.put(i, currentAccountList.get(i));
            }
            for(Account i : currentAccountList.keySet()){
                if(!currentAccountList.get(i).equals("")){ //se gli account hanno il campo 'String' settato -> rimuovi
                    tempList.remove(i);
                }
            }
            gridStud.setItems(tempList.keySet());
        });

        vert.add(gridStud, btn);
        return vert;
    }

    //Contenitore per lista studenti da inserire nel gioco Guess
    private VerticalLayout containerListStudentGuess(String width, String height){
        VerticalLayout vert = new VerticalLayout();
        vert.addClassName("gridContainerVertLayout");
        vert.setSpacing(false);

        gridGuess.getStyle().set("width", "100%");
        gridGuess.getStyle().set("height", "80%");

        Button btn = new Button("Avvia");
        btn.addClassName("btnUnderGrid");
        btn.addClickListener(event ->{
            //INSERISCI VINCOLO DI ALMENO 2 utenti

            //Invia un event contenente la lista (account, gameName) a StudentHomeView
            //definire new Event class and listener per questo scopo
            startGameEventBeanPublisher.doStuffAndPublishAnEvent(currentAccountList, true);
        });

        vert.add(gridGuess, btn);
        return vert;
    }

    private VerticalLayout containerListStudentMaty(String width, String height){
        VerticalLayout vert = new VerticalLayout();
        vert.addClassName("gridContainerVertLayout");
        vert.setSpacing(false);

        gridMaty.getStyle().set("width", "100%");
        gridMaty.getStyle().set("height", "80%");

        Button btn = new Button("Avvia");
        btn.addClassName("btnUnderGrid");
        btn.addClickListener(event ->{
            //INSERISCI VINCOLO DI ALMENO 2 utenti

            //Invia un event contenente la lista (account, gameName) a StudentHomeView
            //definire new Event class and listener per questo scopo
            startGameEventBeanPublisher.doStuffAndPublishAnEvent(currentAccountList, true);
        });

        vert.add(gridMaty, btn);
        return vert;
    }

    private VerticalLayout containerListStudentNewGame(String width, String height){
        VerticalLayout vert = new VerticalLayout();
        vert.addClassName("gridContainerVertLayout");
        vert.setSpacing(false);

        gridNewGame.getStyle().set("width", "100%");
        gridNewGame.getStyle().set("height", "80%");

        Button btn = new Button("Avvia");
        btn.addClassName("btnUnderGrid");

        vert.add(gridNewGame, btn);
        return vert;
    }


    private Image generateImage(Account account) {
        Long id = account.getId();
        StreamResource sr = new StreamResource("user", () ->  {
            Account attached = accRep.findWithPropertyPictureAttachedById(id);
            return new ByteArrayInputStream(attached.getProfilePicture());
        });
        sr.setContentType("image/png");
        Image image = new Image(sr, "profile-picture");
        return image;
    }

    //Aggiorna il contenuto del campo 'value' con il gioco impostato dal teacher
    public void updateAccountListHashMap(Account acc, String game){
        currentAccountList.replace(acc, game);
    }

    //Implementazione 'BroadcasterListener'
    @Override
    public void redirectToGuess(){
        //no implement
    }

    @Override
    public void redirectToMaty(){
        //no implement
    }

    @Override
    //Aggiorna e fondi le liste di account (soluzione del bug: 'Aggiorna' button)
    public void updateAndMergeAccountList(){
        Map<Account, String> actualList = Broadcaster.getAccountListReceive();
        for(Account i : actualList.keySet()){
            if(currentAccountList.containsKey(i)){ //se il nuovo account e' gia' presente in actualList
                actualList.replace(i, currentAccountList.get(i)); //modifica il value 'Game' degli account esistenti in currentList
            }
        }
        currentAccountList = actualList; //aggiorna lista corrente con 'actualList' che contiene nuovi utenti connessi
        updateAllGrid();
    }

    private void updateAllGrid(){
        //Aggiorna 'Grid' Studenti collegati
        Map<Account, String> tempList = new HashMap<>();
        for(Account i : currentAccountList.keySet()){
            tempList.put(i, currentAccountList.get(i));
        }
        for(Account i : currentAccountList.keySet()){
            if(!currentAccountList.get(i).equals("")){ //se gli account hanno il campo 'String' settato -> rimuovi
                tempList.remove(i);
            }
        }
        getUI().get().access(() -> {
            gridStud.setItems(tempList.keySet());
        });

    }

}
