package controller;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.domain.UserFriendshipDTO;
import socialnetwork.service.EventService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;

public class RequestsController {
    @FXML
    TableView<UserFriendshipDTO> tableView;
    @FXML
    TableColumn<UserFriendshipDTO, String> tableColumnFName;
    @FXML
    TableColumn<UserFriendshipDTO,String> tableColumnLName;
    @FXML
    TableColumn<UserFriendshipDTO,String> tableColumnEmail;
    @FXML
    TableColumn<UserFriendshipDTO,LocalDateTime> tableColumnDate;
    @FXML
    TableColumn<UserFriendshipDTO,String> tableColumnStatus;
    @FXML
    Button buttonAccept;
    @FXML
    Button buttonReject;
    ObservableList<UserFriendshipDTO> model = FXCollections.observableArrayList();
    private Stage dialogStage;
    private UserService usrv;
    private FriendshipService fsrv;
    private MessageService msrv;
    private User logged;
    private EventService esrv;

    public void setFields(UserService usrv, FriendshipService fsrv, MessageService msrv, EventService esrv, User logged, Stage stage) {
        this.usrv=usrv;
        this.fsrv=fsrv;
        this.msrv=msrv;
        this.esrv=esrv;
        this.logged=logged;
        this.dialogStage=stage;
        initModel();
        tableView.setPlaceholder(new Label("You have no friend requests"));
    }

    @FXML
    public void initialize() {
        tableColumnFName.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("fname"));
        tableColumnLName.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("lname"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("email"));
        tableColumnDate.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,LocalDateTime>("date"));
        tableColumnStatus.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("status"));
        tableView.setItems(model);
        tableView.getSelectionModel().selectedItemProperty().addListener(x->handleSelection());
    }

    public void setButtons(Boolean b){
        buttonAccept.setVisible(b);
        buttonAccept.setDisable(!b);
        buttonReject.setVisible(b);
        buttonReject.setDisable(!b);
    }

    private void handleSelection() {
        if(tableView.getSelectionModel().isEmpty()){
            setButtons(false);
        }
        else{
            setButtons(true);
        }
    }

    private void initModel() {
        model.clear();
        for(Friendship f: fsrv.getAll()) {
            if (f.getStatus() != null && f.getStatus().equals("Pending"))
                if (logged.getId().equals(f.getId().getRight())) {
                    User u=usrv.getOne(f.getId().getLeft());
                    UserFriendshipDTO ufd=new UserFriendshipDTO(u.getFirstName(),u.getLastName(),u.getEmail(),f.getDate(),f.getStatus());
                    model.add(ufd);
                }
        }
    }

    public void answerRequest(String answer){
        if(tableView.getSelectionModel().isEmpty())
            MessageAlert.showErrorMessage(null,"You have to select a request!");
        else {
            UserFriendshipDTO selected = tableView.getSelectionModel().getSelectedItem();
            User user = usrv.getByEmail(selected.getEmail());
            Tuple<Long, Long> t = new Tuple(logged.getId(), user.getId());
            Friendship f = fsrv.getOne(t);
            f.setStatus(answer);
            f.setDate(LocalDateTime.now());
            fsrv.update(f);
            initModel();
        }
    }

    public void handleAccept() {
        answerRequest("Accepted");
    }

    public void handleReject() {
        answerRequest("Rejected");
    }

    public void handleCancel() throws IOException {
        FXMLLoader dbLoader = new FXMLLoader();
        dbLoader.setLocation(getClass().getResource("/views/DashboardView.fxml"));
        AnchorPane dbLayout = dbLoader.load();
        dialogStage.setScene(new Scene(dbLayout));
        dialogStage.setTitle("Your page");
        DashboardController dbController = dbLoader.getController();
        dbController.setFields(logged,usrv,fsrv,msrv,esrv, dialogStage);
    }
}
