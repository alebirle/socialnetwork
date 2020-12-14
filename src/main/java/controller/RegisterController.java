package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import security.MyBcrypt;
import socialnetwork.domain.User;
import socialnetwork.service.EventService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;

import java.io.IOException;

public class RegisterController {
    @FXML
    private TextField textFName;
    @FXML
    private TextField textLName;
    @FXML
    private TextField textNewEmail;
    @FXML
    private PasswordField textNewPassword;
    @FXML
    private PasswordField textConfirmPassword;
    private User logged;
    private UserService usrv;
    private FriendshipService fsrv;
    private MessageService msrv;
    private Stage primaryStage;
    private EventService esrv;

    public void handleRegister() {
        if(textNewEmail.getText().isEmpty()||textNewPassword.getText().isEmpty()||textConfirmPassword.getText().isEmpty()||textFName.getText().isEmpty()||textLName.getText().isEmpty()){
            MessageAlert.showErrorMessage(null,"The fields can't be empty!");
            return;
        }
        if(textNewPassword.getText().length()<6){
            MessageAlert.showErrorMessage(null,"The password is too short!");
            return;
        }
        if(!textNewPassword.getText().equals(textConfirmPassword.getText())){
            MessageAlert.showErrorMessage(null,"The passwords do not match!");
            return;
        }
        User u=new User(textFName.getText(),textLName.getText(),textNewEmail.getText(), MyBcrypt.hash(textNewPassword.getText()));
        try {
            User rez = usrv.addUser(u);
            if(rez==null)
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Account created succesfully!","You are now registered.");
            else {
                MessageAlert.showErrorMessage(null, "An account with this email address already exists!");
                return;
            }
            logged=usrv.getOne(u.getId());
            goToDashboard(logged,usrv,fsrv);
        }
        catch(Exception e){
            MessageAlert.showErrorMessage(null,e.getMessage());
        }
    }

    public void goToDashboard(User logged,UserService usrv, FriendshipService fsrv) throws IOException {
        FXMLLoader dbLoader = new FXMLLoader();
        dbLoader.setLocation(getClass().getResource("/views/DashboardView.fxml"));
        AnchorPane dbLayout = dbLoader.load();
        primaryStage.setScene(new Scene(dbLayout));
        primaryStage.setTitle("Your page");
        DashboardController dbController = dbLoader.getController();
        dbController.setFields(logged,usrv,fsrv,msrv,esrv, primaryStage);
    }

    public void setFields(User logged, UserService usrv, FriendshipService fsrv, MessageService msrv, EventService esrv, Stage primaryStage) {
        this.logged=logged;
        this.usrv=usrv;
        this.fsrv=fsrv;
        this.msrv=msrv;
        this.esrv=esrv;
        this.primaryStage=primaryStage;
        textFName.setFocusTraversable(false);
        textLName.setFocusTraversable(false);
        textNewEmail.setFocusTraversable(false);
        textNewPassword.setFocusTraversable(false);
        textConfirmPassword.setFocusTraversable(false);
    }

    public void handleCancel() throws IOException {
        FXMLLoader accountLoader = new FXMLLoader();
        accountLoader.setLocation(getClass().getResource("/views/AccountView.fxml"));
        AnchorPane dbLayout = accountLoader.load();
        primaryStage.setScene(new Scene(dbLayout));
        AccountController dbController = accountLoader.getController();
        dbController.setFields(usrv,fsrv,msrv,esrv, logged, primaryStage);
    }
}
