package com.example.demo.users.controller;

import com.example.demo.users.broadcaster.BroadcastListenerTeacher;
import com.example.demo.users.broadcaster.Broadcaster;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import java.util.*;

@Push
@Route("ControllerGestStud")
@HtmlImport("style.html")
@StyleSheet("frontend://stile/stile.css")
@StyleSheet("frontend://stile/gestStudStyle.css")
@PageTitle("Gestione Studenti")
public class GestioneStudentUI extends HorizontalLayout implements BroadcastListenerTeacher {

    //instance field
    private AccountRepository accRep;
    private Account account;
    private Grid<Account> gridStud = new Grid<>(Account.class);
    private Grid<Account> gridGuess = new Grid<>(Account.class);
    private Grid<Account> gridMaty = new Grid<>(Account.class);
    private Grid<Account> gridNewGame = new Grid<>(Account.class);
    private List<Account> draggedItems; //elementi soggetti a DnD
    private Grid<Account> dragSource; //sorgente da cui 'parte' il DnD
    private StartGameEventBeanPublisher startGameEventBeanPublisher;
    private Map<Account, String> currentAccountList = new HashMap<>(); //lista corrente di account list
    private int countGuessUser = 0;
    private int countMatyUser = 0;
    private Account removed = new Account(); //viene rimosso al piu' un account alla volta (cioe' un event alla volta)

    public GestioneStudentUI(/*@Autowired*/ StartGameEventBeanPublisher startGameEventPublisher){
        try {
            accRep = (AccountRepository) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("rep");
            account = (Account) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
            startGameEventBeanPublisher = startGameEventPublisher;

            setId("GestioneStudentUI"); //setta id del root element di questo component


            //Registra un teacher listener
            Broadcaster.registerTeacherForGestStud(account, this);

            getStyle().set("height", "100%"); //per nav bar verticale
            getStyle().set("width", "100%");
            AppBarUI appBar = new AppBarUI("Gestione studenti", false, false); //nome pagina corrente
            add(appBar);

            Paragraph guidetxt = new Paragraph("Selezionate uno studente dalla lista e trascinatelo nel gioco che desiderate." +
                    "Premere il pulsante 'Gioca' per iniziare una nuova partita con il gruppo di studenti che è stato impostato.");
            guidetxt.addClassName("guideText");
            guidetxt.getStyle().set("left", NavBarVertical.NAVBAR_WIDTH);

            updateAndMergeAccountList();
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

        vert.add(gridStud);
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
            startGame();
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
            startGame();
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

    //Inizia la partita di Guess o Maty quando teacher preme 'Invia'
    private void startGame(){
        updateCountofGuessAndMatyUser();
        if(countGuessUser > 1){ //vincolo di almeno due utenti inseriti nella grid
            //invia un event contenente la lista di studenti collegati che devono giocare con Guess
            startGameEventBeanPublisher.doStuffAndPublishAnEvent(currentAccountList, true);
            Broadcaster.setIsGuessStart(true);
        }else if(countMatyUser > 1){ //vincolo di almeno due utenti inseriti nella grid
            //invia un event contenente la lista di studenti collegati che devono giocare con Guess
            startGameEventBeanPublisher.doStuffAndPublishAnEvent(currentAccountList, true);
            Broadcaster.setIsMatyStart(true);
        }else{
            Label content = new Label("Almeno due utenti per poter iniziare la partita");
            Notification notification = new Notification(content);
            notification.setDuration(3000);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
        }
    }

    //Aggiorna il contenuto del campo 'value' con il gioco impostato dal teacher
    private void updateAccountListHashMap(Account acc, String game){
        currentAccountList.replace(acc, game);
    }

    private void updateCountofGuessAndMatyUser(){
        countGuessUser = 0;
        countMatyUser = 0;
        for(Account i : currentAccountList.keySet()){
            String game = currentAccountList.get(i);
            if(game.equals("Guess")){
                countGuessUser++;
            }else if(game.equals("Maty")){
                countMatyUser++;
            }
        }
    }

    //Implementazione 'BroadcastListenerTeacher'
    @Override
    public void startGameInBackground(String game){
        //no implement
    }

    @Override
    //Aggiorna e fondi le liste di account
    public void updateAndMergeAccountList(){
        Map<Account, String> actualList = Broadcaster.getAccountListReceive();
        for(Account i : actualList.keySet()){
            if(currentAccountList.containsKey(i)){ //se il nuovo account e' gia' presente in actualList
                actualList.replace(i, currentAccountList.get(i)); //modifica il value 'Game' degli account esistenti in currentList
            }
        }
        currentAccountList = actualList; //aggiorna lista corrente con 'actualList' che contiene nuovi utenti connessi
        updateGridStudentCollegati();
    }

    //Aggiorna 'Grid' Studenti collegati (rimuove dalla lista gli account gia' presenti nelle altre grid)
    private void updateGridStudentCollegati(){
        Map<Account, String> tempList = new HashMap<>();
        for(Account i : currentAccountList.keySet()){
            tempList.put(i, currentAccountList.get(i));
        }
        for(Account i : currentAccountList.keySet()){
            if(!currentAccountList.get(i).equals("")){ //se gli account hanno il campo 'String' settato -> rimuovi
                tempList.remove(i);
            }
        }
        if(getUI().isPresent()) {  //se e' presente una UI attached a questo component
            getUI().get().access(() -> {
                gridStud.setItems(tempList.keySet());
            });
        }else{
            gridStud.setItems(tempList.keySet());
        }
    }

    @Override
    public void removeAccountFromAllGrid(){
        Map<Account, String> receiveList = Broadcaster.getAccountListReceive();

        for(Account i: currentAccountList.keySet()){
            if(!receiveList.containsKey(i)){    //account i e' stato rimosso -> elimina da currentAccountList
                currentAccountList.remove(i);
                removed = i;
            }
        }

        if(getUI().isPresent()){
            getUI().get().access(() -> {
                ListDataProvider<Account> sourceDataProvider = (ListDataProvider<Account>) gridStud.getDataProvider();
                List<Account> sourceItems = new ArrayList<>(sourceDataProvider.getItems());
                sourceItems.remove(removed);
                gridStud.setItems(sourceItems);

                sourceDataProvider = (ListDataProvider<Account>) gridGuess.getDataProvider();
                sourceItems = new ArrayList<>(sourceDataProvider.getItems());
                if(sourceItems.contains(removed)) {
                    sourceItems.remove(removed);
                    gridGuess.setItems(sourceItems);
                }

                sourceDataProvider = (ListDataProvider<Account>) gridMaty.getDataProvider();
                sourceItems = new ArrayList<>(sourceDataProvider.getItems());
                if(sourceItems.contains(removed)) {
                    sourceItems.remove(removed);
                    gridMaty.setItems(sourceItems);
                }
            });
        }
    }

    @Override
    public void removeAccountFromThisGrid(Account a, String gridName){

        if(getUI().isPresent()){
            getUI().get().access(()->{
                if(gridName.equals("Guess")){
                    ListDataProvider<Account> sourceDataProvider = (ListDataProvider<Account>) gridGuess.getDataProvider();
                    List<Account> sourceItems = new ArrayList<>(sourceDataProvider.getItems());
                    sourceItems.remove(a);
                    gridGuess.setItems(sourceItems);
                }else if(gridName.equals("Maty")){
                    ListDataProvider<Account> sourceDataProvider = (ListDataProvider<Account>) gridMaty.getDataProvider();
                    List<Account> sourceItems = new ArrayList<>(sourceDataProvider.getItems());
                    sourceItems.remove(a);
                    gridMaty.setItems(sourceItems);
                }
                currentAccountList.replace(a, currentAccountList.get(a), "");
                updateGridStudentCollegati();
            });
        }
    }

}
