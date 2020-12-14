package controller;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import socialnetwork.domain.Event;
import socialnetwork.domain.User;
import socialnetwork.service.EventService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CreateEventController {
    @FXML
    TextField textName;
    @FXML
    TextArea textDescription;
    @FXML
    DatePicker datepickerFrom;
    @FXML
    DatePicker datepickerTo;
    @FXML
    ComboBox fromHour;
    @FXML
    ComboBox fromMinute;
    @FXML
    ComboBox toHour;
    @FXML
    ComboBox toMinute;
    ObservableList<Integer> hours= FXCollections.observableArrayList(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23);
    ObservableList<Integer> minutes=FXCollections.observableArrayList(0,10,20,30,40,50);
    User logged;
    UserService usrv;
    FriendshipService fsrv;
    MessageService msrv;
    Stage primaryStage;
    EventService esrv;

    @FXML
    public void initialize() {
        fromHour.getItems().addAll(hours);
        toHour.getItems().addAll(hours);
        fromMinute.getItems().addAll(minutes);
        toMinute.getItems().addAll(minutes);
    }

    public void setFields(User logged, UserService usrv, FriendshipService fsrv, MessageService msrv, EventService esrv, Stage primaryStage) {
        this.logged=logged;
        this.usrv=usrv;
        this.fsrv=fsrv;
        this.msrv=msrv;
        this.esrv=esrv;
        this.primaryStage=primaryStage;
        textName.setFocusTraversable(false);
        textDescription.setFocusTraversable(false);
        datepickerFrom.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                setDisable(empty || date.compareTo(today) < 0 );
            }
        });
        datepickerTo.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                setDisable(empty || date.compareTo(today) < 0 );
            }
        });
    }

    public void handleCreate() throws IOException {
        if(textName.getText().isEmpty()||textDescription.getText().isEmpty()||datepickerFrom.getValue()==null||datepickerTo.getValue()==null||fromHour.getSelectionModel().isEmpty()||fromMinute.getSelectionModel().isEmpty()||toHour.getSelectionModel().isEmpty()||toMinute.getSelectionModel().isEmpty())
            MessageAlert.showErrorMessage(null,"Fields can't be empty!");
        else{
            LocalDateTime from=LocalDateTime.of(datepickerFrom.getValue().getYear(),datepickerFrom.getValue().getMonth(),datepickerFrom.getValue().getDayOfMonth(),(int)fromHour.getSelectionModel().getSelectedItem(),(int)fromMinute.getSelectionModel().getSelectedItem());
            LocalDateTime to=LocalDateTime.of(datepickerTo.getValue().getYear(),datepickerTo.getValue().getMonth(),datepickerTo.getValue().getDayOfMonth(),(int)toHour.getSelectionModel().getSelectedItem(),(int)toMinute.getSelectionModel().getSelectedItem());
            if(from.isAfter(to)){
                MessageAlert.showErrorMessage(null,"End date must be after start date!");
                return;
            }
            Event event=new Event(textName.getText(),textDescription.getText(),logged.getId(),from,to);
            esrv.save(event);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Event created","Event created successfully");
            handleCancel();
        }
    }

    public void handleCancel() throws IOException {
        FXMLLoader dbLoader = new FXMLLoader();
        dbLoader.setLocation(getClass().getResource("/views/EventsView.fxml"));
        AnchorPane dbLayout = dbLoader.load();
        primaryStage.setScene(new Scene(dbLayout));
        primaryStage.setTitle("Events");
        EventsController dbController = dbLoader.getController();
        dbController.setFields(logged,usrv,fsrv,msrv,esrv, primaryStage);
    }
}
