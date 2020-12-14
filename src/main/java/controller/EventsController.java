package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    ChoiceBox<String> filter;
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
    }

    private void handleFilter() {
        Predicate<Event> namePredicate = x -> {
            String s = x.getName() + " " + x.getDescription();
            return s.contains(search.getText());
        };
        events.setAll(events.stream().filter(namePredicate).collect(Collectors.toList()));
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
        for(Event e:esrv.getAll()) {
            events.add(e);
        }
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
        esrv.setNotify(logged.getId(), listView.getSelectionModel().getSelectedItem().getId(),false);
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
        esrv.setNotify(logged.getId(), listView.getSelectionModel().getSelectedItem().getId(),true);
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Success!","You will now receive notifications for this event");
        handleSelection();
    }
}
