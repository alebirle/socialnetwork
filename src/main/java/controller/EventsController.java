package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import socialnetwork.domain.Event;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.repository.RepoException;
import socialnetwork.service.EventService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventsController {
    @FXML
    ChoiceBox<String> filterWhich;
    @FXML
    ChoiceBox<String> filterWhen;
    @FXML
    ListView<Event> listView;
    @FXML
    TextField search;
    @FXML
    Button buttonAttend;
    @FXML
    Button buttonDelete;
    @FXML
    Button buttonRemove;
    @FXML
    Button buttonMute;
    @FXML
    Button buttonUnmute;
    @FXML
    Pagination pagination;
    ObservableList<Event> events= FXCollections.observableArrayList();
    User logged;
    UserService usrv;
    FriendshipService fsrv;
    MessageService msrv;
    Stage primaryStage;
    EventService esrv;

    @FXML
    public void initialize() {
        listView.setItems(events);
        search.setFocusTraversable(false);
        listView.getSelectionModel().selectedItemProperty().addListener(x->handleSelection());
        search.textProperty().addListener(x->handleFilter());
        filterWhich.getSelectionModel().selectedItemProperty().addListener(x->handleFilter());
        filterWhen.getSelectionModel().selectedItemProperty().addListener(x->handleFilter());
        pagination.setPageFactory(this::createPage);
        pagination.currentPageIndexProperty().addListener(x->handlePage());
        listView.setPlaceholder(new Label("There are no events"));
    }

    private void handlePage() {
        handleFilter();
    }

    private ListView createPage(Integer integer) {
        return listView;
    }

    private void handleFilter() {
        initEvents();
        if (!search.getText().isEmpty()) {
            Predicate<Event> namePredicate = x -> {
                String s = x.getName() + " " + x.getDescription();
                if(filterWhich.getSelectionModel().getSelectedItem().equals("Yours")&&!x.getOwnerId().equals(logged.getId()))
                    return false;
                if(filterWhich.getSelectionModel().getSelectedItem().equals("Going")&&!esrv.getExists(logged.getId(), x.getId()))
                    return false;
                if(filterWhen.getSelectionModel().getSelectedItem().equals("Past")&&x.getEndAt().isAfter(LocalDateTime.now()))
                    return false;
                if(filterWhen.getSelectionModel().getSelectedItem().equals("Upcoming")&&x.getEndAt().isBefore(LocalDateTime.now()))
                    return false;
                return s.contains(search.getText());
            };
            List<Event> e=new ArrayList<>();
            esrv.getAll().forEach(e::add);
            events.setAll(e.stream().filter(namePredicate).collect(Collectors.toList()));
        }
        if(events.isEmpty())
            pagination.setPageCount(1);
        else
            pagination.setPageCount((int)Math.ceil((float)esrv.getNr(logged,filterWhich.getSelectionModel().getSelectedItem(),filterWhen.getSelectionModel().getSelectedItem())/(float)3));
    }

    private void initButtons(List<Boolean> b){
        buttonAttend.setVisible(b.get(0));
        buttonAttend.setDisable(b.get(1));
        buttonDelete.setVisible(b.get(2));
        buttonDelete.setDisable(b.get(3));
        buttonRemove.setVisible(b.get(4));
        buttonRemove.setDisable(b.get(5));
    }
    private void initMute(List<Boolean> b){
        buttonMute.setVisible(b.get(0));
        buttonMute.setDisable(b.get(1));
        buttonUnmute.setVisible(b.get(2));
        buttonUnmute.setDisable(b.get(3));
    }

    private void handleSelection() {
        if(listView.getSelectionModel().getSelectedItem()==null) {
            initButtons(Arrays.asList(false,true,false,true,false,true));
            initMute(Arrays.asList(false,true,false,true));
            return;
        }
        Event event=listView.getSelectionModel().getSelectedItem();
        if(event.getOwnerId().equals(logged.getId())){
            initButtons(Arrays.asList(false,true,true,false,false,true));
            if(esrv.getNotifications(logged.getId(), event.getId())) {
                initMute(Arrays.asList(true,false,false,true));
            }
            else{
                initMute(Arrays.asList(false,true,true,false));
            }
        }
        else {
            if (esrv.getExists(logged.getId(), event.getId())) {
                initButtons(Arrays.asList(false,true,false,true,true,false));
                if (esrv.getNotifications(logged.getId(), event.getId())) {
                    initMute(Arrays.asList(true,false,false,true));
                }
                else{
                    initMute(Arrays.asList(false,true,true,false));
                }
            }
            else{
                initButtons(Arrays.asList(true,false,false,true,false,true));
                initMute(Arrays.asList(false,true,false,true));
            }
        }
    }

    public void initEvents(){
        events.clear();
        listView.getSelectionModel().clearSelection();
        /*for(Event e:esrv.getAll()) {
            events.add(e);
        }
        if(filterWhich.getSelectionModel().getSelectedItem().equals("Yours")){
            events.setAll(events.stream().filter(x->x.getOwnerId().equals(logged.getId())).collect(Collectors.toList()));
        }
        if(filterWhich.getSelectionModel().getSelectedItem().equals("Going")){
            events.setAll(events.stream().filter(x-> esrv.getExists(logged.getId(), x.getId())).collect(Collectors.toList()));
        }
        if(filterWhen.getSelectionModel().getSelectedItem().equals("Upcoming")){
            events.setAll(events.stream().filter(x-> x.getEndAt().isAfter(LocalDateTime.now())).collect(Collectors.toList()));
        }
        if(filterWhen.getSelectionModel().getSelectedItem().equals("Past")){
            events.setAll(events.stream().filter(x-> x.getEndAt().isBefore(LocalDateTime.now())).collect(Collectors.toList()));
        }*/
        for(Event e:esrv.getSome(logged,filterWhich.getSelectionModel().getSelectedItem()+" "+filterWhen.getSelectionModel().getSelectedItem()+" "+String.valueOf(pagination.getCurrentPageIndex()*3)))
            events.add(e);
        events.sort((x,y)->{if(x.getEndAt().isBefore(y.getEndAt())) return 1; else return 0;});
        if(events.isEmpty())
            pagination.setPageCount(1);
        else
            pagination.setPageCount((int)Math.ceil((float)esrv.getNr(logged,filterWhich.getSelectionModel().getSelectedItem(),filterWhen.getSelectionModel().getSelectedItem())/(float)3));
    }

    public void setFields(User logged, UserService usrv, FriendshipService fsrv, MessageService msrv, EventService esrv, Stage primaryStage) {
        this.logged=logged;
        this.usrv=usrv;
        this.fsrv=fsrv;
        this.msrv=msrv;
        this.esrv=esrv;
        this.primaryStage=primaryStage;
        initEvents();
    }

    public void handleRemove() {
        esrv.removeParticipant(logged.getId(), listView.getSelectionModel().getSelectedItem().getId());
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Success!","You are no longer attending this event");
        handleSelection();
    }

    public void handleMute() {
        esrv.setNotify(logged.getId(), listView.getSelectionModel().getSelectedItem().getId(),false,10);
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Success!","You will no longer receive notifications for this event");
        handleSelection();
    }

    public void handleCreate() throws IOException {
        FXMLLoader dbLoader = new FXMLLoader();
        dbLoader.setLocation(getClass().getResource("/views/CreateEvent.fxml"));
        AnchorPane dbLayout = dbLoader.load();
        primaryStage.setScene(new Scene(dbLayout));
        primaryStage.setTitle("Create event");
        CreateEventController dbController = dbLoader.getController();
        dbController.setFields(logged,usrv,fsrv,msrv,esrv, primaryStage);
    }

    public void handleDelete() {
        try {
            esrv.delete(listView.getSelectionModel().getSelectedItem());
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Success!","Event deleted");
            initEvents();
        }
        catch (RepoException e){
            MessageAlert.showErrorMessage(null,e.getMessage());
        }
    }

    public void handleCancel() throws IOException {
        FXMLLoader dbLoader = new FXMLLoader();
        dbLoader.setLocation(getClass().getResource("/views/DashboardView.fxml"));
        AnchorPane dbLayout = dbLoader.load();
        primaryStage.setScene(new Scene(dbLayout));
        primaryStage.setTitle("Your page");
        DashboardController dbController = dbLoader.getController();
        dbController.setFields(logged,usrv,fsrv,msrv,esrv, primaryStage);
    }

    public void handleAttend() {
        esrv.addParticipant(logged.getId(), listView.getSelectionModel().getSelectedItem().getId());
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Success!","You are now attending this event");
        handleSelection();
    }

    public void handleUnmute() {
        esrv.setNotify(logged.getId(), listView.getSelectionModel().getSelectedItem().getId(),true,10);
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Success!","You will now receive notifications for this event");
        handleSelection();
    }
}
