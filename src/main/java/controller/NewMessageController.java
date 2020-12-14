package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Message;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.service.EventService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NewMessageController {
    @FXML
    ListView listView;
    @FXML
    TextField textSearch;
    @FXML
    TextField textMessage;
    private User logged;
    private UserService usrv;
    private FriendshipService fsrv;
    private MessageService msrv;
    private Stage primaryStage;
    ObservableList<CheckBox> users = FXCollections.observableArrayList();
    private EventService esrv;

    @FXML
    public void initialize() {
        listView.setItems(users);
        textSearch.textProperty().addListener(x->handleFilter());
    }

    private void handleFilter() {
        if(textSearch.getText().isEmpty()){
            initFriends();
        }
        else {
            Predicate<CheckBox> namePredicate = x -> {
                String s = x.getText();
                return s.contains(textSearch.getText());
            };
            users.setAll(users
                    .stream()
                    .filter(namePredicate)
                    .collect(Collectors.toList()));
        }
    }

    public void setFields(User l, UserService usrv, FriendshipService fsrv, MessageService msrv, EventService esrv, Stage primaryStage) {
        this.logged=l;
        this.usrv=usrv;
        this.fsrv=fsrv;
        this.msrv=msrv;
        this.esrv=esrv;
        this.primaryStage=primaryStage;
        initFriends();
        textSearch.setFocusTraversable(false);
        textMessage.setFocusTraversable(false);
        listView.setPlaceholder(new Label("You have no friends so far"));
    }

    private void initFriends() {
        List<Friendship> fr = new ArrayList<>();
        fsrv.getAll().forEach(fr::add);
        fr.stream()
                .filter(x->x.getStatus()!=null&&x.getStatus().equals("Accepted"))
                .filter(x->x.getId().getLeft().equals(logged.getId()))
                .forEach(x->{
                    User f=usrv.getOne(x.getId().getRight());
                    users.add(new CheckBox(f.getFirstName()+" "+f.getLastName()+" "+f.getEmail()));
                });
        fr.stream()
                .filter(x->x.getStatus()!=null&&x.getStatus().equals("Accepted"))
                .filter(x->x.getId().getRight().equals(logged.getId()))
                .forEach(x->{
                    User f=usrv.getOne(x.getId().getLeft());
                    users.add(new CheckBox(f.getFirstName()+" "+f.getLastName()+" "+f.getEmail()));
                });
    }

    public void handleCancel() throws IOException {
        goToDashboard();
    }

    public void handleSend() throws IOException {
        if(!textMessage.getText().isEmpty()) {
            List<User> to = new ArrayList<>();
            for (CheckBox friends : users) {
                if (friends.isSelected()){
                    String[] s=friends.getText().split(" ");
                    User u=usrv.getByEmail(s[s.length-1]);
                    to.add(u);
                }
            }
            if (to.isEmpty())
                MessageAlert.showErrorMessage(null, "You must select at least one friend!");
            else {
                Message m = new Message(logged, to, textMessage.getText());
                msrv.save(m);
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Message sent!","Your message was sent succesfully.");
                goToDashboard();
            }
        }
    }

    private void goToDashboard() throws IOException {
        FXMLLoader dbLoader = new FXMLLoader();
        dbLoader.setLocation(getClass().getResource("/views/DashboardView.fxml"));
        AnchorPane dbLayout = dbLoader.load();
        primaryStage.setScene(new Scene(dbLayout));
        primaryStage.setTitle("Your page");
        DashboardController dbController = dbLoader.getController();
        dbController.setFields(logged,usrv,fsrv,msrv,esrv, primaryStage);
    }
}
