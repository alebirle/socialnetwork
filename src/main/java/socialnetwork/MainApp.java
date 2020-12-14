package socialnetwork;

import controller.AccountController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.config.ApplicationContext;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.FriendshipValidator;
import socialnetwork.domain.validators.MessageValidator;
import socialnetwork.domain.validators.UserValidator;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.*;
import socialnetwork.service.EventService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;

import java.io.IOException;

public class MainApp extends Application {
    final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
    final String username= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.username");
    final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");
    Repository<Long, User> userDbRepository = new UserDbRepository(url,username, password,  new UserValidator());
    Repository<Tuple<Long,Long>, Friendship> friendshipDbRepository=new FriendshipDbRepository(url,username,password,new FriendshipValidator());
    Repository<Long, Message> messageDbRepository=new MessageDbRepository(url,username,password,new MessageValidator());
    Repository<Long, Event> eventDbRepository=new EventDbRepository(url,username,password);
    UserEventDbRepository userEventDbRepository=new UserEventDbRepository(url,username,password);
    UserService usrv=new UserService(userDbRepository);
    FriendshipService fsrv=new FriendshipService(friendshipDbRepository);
    MessageService msrv=new MessageService(messageDbRepository);
    EventService esrv=new EventService(eventDbRepository,userEventDbRepository);
    public User logged=new User();

    private void initView(Stage primaryStage) throws IOException {

        FXMLLoader accountLoader = new FXMLLoader();
        accountLoader.setLocation(getClass().getResource("/views/AccountView.fxml"));
        AnchorPane accountLayout = accountLoader.load();
        primaryStage.setScene(new Scene(accountLayout));
        primaryStage.setTitle("Login");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("/images/star-icon.png"));
        AccountController accountController = accountLoader.getController();
        accountController.setFields(usrv,fsrv,msrv,esrv,logged, primaryStage);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initView(primaryStage);
        primaryStage.setWidth(800);
        primaryStage.setHeight(450);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
