package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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

public class AccountController {
    @FXML
    private TextField textEmail;
    @FXML
    private PasswordField textPassword;
    private UserService usrv;
    private FriendshipService fsrv;
    private MessageService msrv;
    private User logged;
    private Stage primaryStage;
    private EventService esrv;

    @FXML
    public void handleLogin() throws IOException {
        if(textEmail.getText().isEmpty()||textPassword.getText().isEmpty()){
            MessageAlert.showErrorMessage(null,"Email and password can't be empty!");
            return;
        }
        try {
            logged = usrv.getByEmail(textEmail.getText());
        }
        catch(Exception e){
            MessageAlert.showErrorMessage(null,e.getMessage());
            return;
        }
        if(logged==null){
            MessageAlert.showErrorMessage(null,"There's no account with that email address");
            return;
        }
        if(logged.getPassword().contains("$2a$")){
            if(!MyBcrypt.verifyHash(textPassword.getText(),logged.getPassword())){
                MessageAlert.showErrorMessage(null,"Email and password don't match!");
                return;
            }
        }
        else{
            if(!logged.getPassword().equals(textPassword.getText())){
                MessageAlert.showErrorMessage(null,"Email and password don't match!");
                return;
            }
        }
        goToDashboard(logged,usrv,fsrv);
    }

    @FXML
    public void handleRegister() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/RegisterView.fxml"));
        AnchorPane layout = loader.load();
        primaryStage.setScene(new Scene(layout));
        primaryStage.setTitle("Register");
        RegisterController controller = loader.getController();
        controller.setFields(logged,usrv,fsrv,msrv,esrv, primaryStage);
    }

    public void goToDashboard(User logged,UserService usrv, FriendshipService fsrv) throws IOException{
        FXMLLoader dbLoader = new FXMLLoader();
        dbLoader.setLocation(getClass().getResource("/views/DashboardView.fxml"));
        AnchorPane dbLayout = dbLoader.load();
        primaryStage.setScene(new Scene(dbLayout));
        primaryStage.setTitle("Your page");
        DashboardController dbController = dbLoader.getController();
        dbController.setFields(logged,usrv,fsrv,msrv,esrv, primaryStage);
    }

    public void setFields(UserService usrv, FriendshipService fsrv, MessageService msrv, EventService esrv, User logged, Stage primaryStage) {
        this.usrv=usrv;
        this.logged=logged;
        this.primaryStage=primaryStage;
        this.fsrv=fsrv;
        this.msrv=msrv;
        this.esrv=esrv;
        textEmail.setFocusTraversable(false);
        textPassword.setFocusTraversable(false);
    }
}
