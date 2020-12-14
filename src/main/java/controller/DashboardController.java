package controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.domain.Event;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.domain.UserFriendshipDTO;
import socialnetwork.service.EventService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;
import utils.UsersChangeEvent;

import javax.swing.text.html.ListView;
import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import utils.Observer;


public class DashboardController implements Observer {
    @FXML
    Label welcome;
    @FXML
    TableView<UserFriendshipDTO> tableView;
    @FXML
    TableColumn<UserFriendshipDTO, String> tableColumnFName;
    @FXML
    TableColumn<UserFriendshipDTO, String> tableColumnLName;
    @FXML
    TableColumn<UserFriendshipDTO, String> tableColumnEmail;
    @FXML
    TableColumn<UserFriendshipDTO,String> tableColumnStatus;
    @FXML
    TextField textSearch;
    @FXML
    Button buttonSendRequest;
    @FXML
    Button buttonCancelRequest;
    @FXML
    Button buttonRemoveFriend;
    @FXML
    Button buttonShowMessages;
    @FXML
    CheckBox friends;
    @FXML
    Pagination pagination;
    @FXML
    MenuButton notifications;
    @FXML
    Label noOfNotifications;
    private User logged;
    private UserService usrv;
    private FriendshipService fsrv;
    private MessageService msrv;
    ObservableList<UserFriendshipDTO> users = FXCollections.observableArrayList();
    private Stage primaryStage;
    private EventService esrv;

    public void initFriends(){
        users.clear();
        List<Friendship> fr = new ArrayList<>();
        String s= String.valueOf(pagination.getCurrentPageIndex()*7);
        fsrv.getSome(logged,s).forEach(fr::add);
        //fsrv.getAll().forEach(fr::add);
        fr.stream()
                //.filter(x->x.getStatus()!=null&&x.getStatus().equals("Accepted"))
                .filter(x->x.getId().getLeft().equals(logged.getId()))
                .forEach(x->{
                    User f=usrv.getOne(x.getId().getRight());
                    UserFriendshipDTO uf=new UserFriendshipDTO(f.getFirstName(),f.getLastName(),f.getEmail(),x.getDate(),x.getStatus());
                    if(x.getStatus().equals("Accepted"))
                        uf.setStatus("Friends");
                    if(x.getStatus().equals("Rejected"))
                        uf.setStatus("Not friends");
                    if(x.getStatus().equals("Pending"))
                        uf.setStatus("Request sent");
                    users.add(uf);
                });
        fr.stream()
                //.filter(x->x.getStatus()!=null&&x.getStatus().equals("Accepted"))
                .filter(x->x.getId().getRight().equals(logged.getId()))
                .forEach(x->{
                    User f=usrv.getOne(x.getId().getLeft());
                    UserFriendshipDTO uf=new UserFriendshipDTO(f.getFirstName(),f.getLastName(),f.getEmail(),x.getDate(),x.getStatus());
                    if(x.getStatus().equals("Accepted"))
                        uf.setStatus("Friends");
                    if(x.getStatus().equals("Rejected"))
                        uf.setStatus("Not friends");
                    if(x.getStatus().equals("Pending"))
                        uf.setStatus("Request sent");
                    users.add(uf);
                });
        if(users.isEmpty())
            pagination.setPageCount(1);
        else
            pagination.setPageCount((int)Math.ceil((float)users.size()/(float)8));
    }

    @FXML
    public void initialize() {
        tableColumnFName.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("fname"));
        tableColumnLName.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("lname"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("email"));
        tableColumnStatus.setCellValueFactory(new PropertyValueFactory<UserFriendshipDTO,String>("status"));
        tableView.setItems(users);
        textSearch.textProperty().addListener(x->handleFilter());
        tableView.getSelectionModel().selectedItemProperty().addListener(x->handleSelection());
        friends.selectedProperty().addListener(x->handleCheckBox());
        textSearch.setFocusTraversable(false);
        friends.setFocusTraversable(false);
        pagination.currentPageIndexProperty().addListener(x->handlePage());
        pagination.setPageFactory(this::createPage);
        notifications.setOnShowing(x->{noOfNotifications.setVisible(false);});
    }

    private void initNotifications(){
        notifications.getItems().clear();
        for(Event e:esrv.getAll()){
            if(esrv.getExists(logged.getId(), e.getId())){
                if(e.getEndAt().isAfter(LocalDateTime.now())){
                    if(e.getStartAt().isBefore(LocalDateTime.now()))
                        notifications.getItems().add(new MenuItem("Event '"+e.getName()+"' is happening now."));
                    else{
                        Long days= ChronoUnit.DAYS.between(LocalDateTime.now(),e.getStartAt());
                        if(days.equals(0))
                            notifications.getItems().add(new MenuItem("Event '"+e.getName()+"' is today"));
                        else
                            if(days.equals(1))
                                notifications.getItems().add(new MenuItem("Event '"+e.getName()+"' is in one day"));
                            else
                                if(days<7)
                                    notifications.getItems().add(new MenuItem("Event '"+e.getName()+"' is in "+days+" days"));
                    }
                }
            }
        }
        if(notifications.getItems().size()==0){
            noOfNotifications.setVisible(false);
            notifications.getItems().add(new MenuItem("You don't have any notifications"));
        }
        else{
            noOfNotifications.setVisible(true);
            noOfNotifications.setText(" "+notifications.getItems().size()+" ");
        }
    }

    private void handlePage() {
        if(friends.isSelected())
            initFriends();
        else
            handleCheckBox();
    }

    private TableView createPage(Integer integer) {
        return tableView;
    }

    public void setButtons(List<Boolean> b){
        buttonSendRequest.setVisible(b.get(0));
        buttonSendRequest.setDisable(b.get(1));
        buttonRemoveFriend.setVisible(b.get(2));
        buttonRemoveFriend.setDisable(b.get(3));
        buttonCancelRequest.setVisible(b.get(4));
        buttonCancelRequest.setDisable(b.get(5));
        buttonShowMessages.setVisible(b.get(6));
        buttonShowMessages.setDisable(b.get(7));
    }

    private void handleSelection() {
        if(tableView.getSelectionModel().isEmpty()){
            setButtons(Arrays.asList(false,true,false,true,false,true,false,true));
        }
        else {
            UserFriendshipDTO selectedDTO = tableView.getSelectionModel().getSelectedItem();
            User selected= usrv.getByEmail(selectedDTO.getEmail());
            if(selected.getId().equals(logged.getId())){
                setButtons(Arrays.asList(false,true,false,true,false,true,false,true));
            }
            else {
                String status = fsrv.getStatus(logged.getId(), selected.getId());
                if (status.equals("None") || status.equals("Rejected")) {
                    setButtons(Arrays.asList(true, false, false, true, false, true,false,true));
                }
                if (status.equals("Accepted")) {
                    setButtons(Arrays.asList(false, true, true, false, false, true,true,false));
                }
                if (status.equals("Pending")) {
                    setButtons(Arrays.asList(false, true, false, true, true, false,false,true));
                }
            }
        }
    }

    public void noOfPages(){
        if(users.size()==0)
            pagination.setPageCount(1);
        else
            pagination.setPageCount((int)Math.ceil((float)users.size()/(float)7));
    }

    private void handleFilter() {
        if(textSearch.getText().isEmpty()){
            handleCheckBox();
        }
        else {
            Predicate<User> namePredicate = x -> {
                String s = x.getFirstName() + " " + x.getLastName();
                return s.contains(textSearch.getText());
            };
            List<User> u = new ArrayList<>();
            if(friends.isSelected()){
                logged.emptyFriends();
                usrv.getFriends(logged);
                logged.getFriends().forEach(u::add);
            }
            else
                usrv.getAll().forEach(u::add);
            ObservableList<User> u2 = FXCollections.observableArrayList();
            u2.setAll(u
                    .stream()
                    .filter(namePredicate)
                    .collect(Collectors.toList()));
            users.clear();
            setDTO(u2);
            noOfPages();
        }
    }

    public void setFields(User l, UserService usrv, FriendshipService fsrv,MessageService msrv,EventService esrv, Stage primaryStage) {
        this.logged=l;
        welcome.setText("Welcome "+logged.getFirstName()+" "+logged.getLastName()+"!");
        this.usrv=usrv;
        this.fsrv=fsrv;
        this.msrv=msrv;
        this.esrv=esrv;
        this.primaryStage=primaryStage;
        initFriends();
        tableView.setPlaceholder(new Label("No users found"));
        textSearch.setFocusTraversable(false);
        tableView.setFixedCellSize(29.72);
        initNotifications();
    }

    public void handleSendRequest() {
        UserFriendshipDTO selectedDTO = tableView.getSelectionModel().getSelectedItem();
        User selected= usrv.getByEmail(selectedDTO.getEmail());
        Friendship f=new Friendship();
        Tuple<Long,Long> t=new Tuple(logged.getId(),selected.getId());
        f.setId(t);
        Friendship rez=fsrv.addFriendship(f);
        if(rez==null) {
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Request sent!","The friend request was sent succesfully.");
        }
        else
            MessageAlert.showErrorMessage(null,"You already sent a request or you are already friends with this user");
    }

    public void handleRemoveFriend() {
        UserFriendshipDTO selectedDTO = tableView.getSelectionModel().getSelectedItem();
        User selected= usrv.getByEmail(selectedDTO.getEmail());
        Tuple<Long,Long> f=new Tuple(logged.getId(),selected.getId());
        Friendship rez=fsrv.deleteFriendship(f);
        if(rez==null)
            MessageAlert.showErrorMessage(null,"Friendship doesn't exist!");
        else {
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Friendship removed",selected.toString() + " was removed from your friends.");
        }
    }

    public void handleCancelRequest() {
        UserFriendshipDTO selectedDTO = tableView.getSelectionModel().getSelectedItem();
        User selected= usrv.getByEmail(selectedDTO.getEmail());
        Tuple<Long,Long> f=new Tuple(logged.getId(),selected.getId());
        Friendship rez=fsrv.deleteFriendship(f);
        if(rez==null)
            MessageAlert.showErrorMessage(null,"Request doesn't exist!");
        else {
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Request removed","Request to "+selected.toString() + " removed.");
        }
    }

    public void handleShowRequests() {
        try {
            FXMLLoader requestsLoader = new FXMLLoader();
            requestsLoader.setLocation(getClass().getResource("/views/RequestsView.fxml"));
            AnchorPane layout = (AnchorPane) requestsLoader.load();
            primaryStage.setScene(new Scene(layout));
            primaryStage.setTitle("Friend requests");
            RequestsController requests = requestsLoader.getController();
            requests.setFields(usrv,fsrv, msrv,esrv, logged,primaryStage);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void handleLogout() throws IOException {
        logged=null;
        FXMLLoader accountLoader = new FXMLLoader();
        accountLoader.setLocation(getClass().getResource("/views/AccountView.fxml"));
        AnchorPane dbLayout = accountLoader.load();
        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(dbLayout));
        AccountController dbController = accountLoader.getController();
        dbController.setFields(usrv,fsrv,msrv,esrv, logged, primaryStage);
    }

    public void handleActivity() throws IOException {
        FXMLLoader requestsLoader = new FXMLLoader();
        requestsLoader.setLocation(getClass().getResource("/views/ActivityView.fxml"));
        AnchorPane layout = (AnchorPane) requestsLoader.load();
        primaryStage.setScene(new Scene(layout));
        primaryStage.setTitle("Your activity");
        ActivityController activity = requestsLoader.getController();
        activity.setFields(usrv,fsrv, msrv,esrv, logged,primaryStage);
    }

    public void handleShowMessages() {
        try {
            FXMLLoader messagesLoader = new FXMLLoader();
            messagesLoader.setLocation(getClass().getResource("/views/MessagesView.fxml"));
            AnchorPane layout = messagesLoader.load();
            primaryStage.setScene(new Scene(layout));
            primaryStage.setTitle("Messages");
            MessagesController m = messagesLoader.getController();
            m.setFields(usrv, fsrv, msrv,esrv, logged, usrv.getByEmail(tableView.getSelectionModel().getSelectedItem().getEmail()), primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        initialize();
    }

    public void handleCheckBox() {
        if(friends.isSelected()){
            users.clear();
            initFriends();
        }
        else{
            users.clear();
            List<User> u=new ArrayList<>();
            usrv.getSome(logged, String.valueOf(pagination.getCurrentPageIndex()*7)).forEach(u::add);
            setDTO(u);
            //usrv.getAll().forEach(users::add);
            pagination.setPageCount((int) Math.ceil((float)StreamSupport.stream(usrv.getAll().spliterator(), false).count()/(float)7));
        }
        if(!textSearch.getText().isEmpty())
            handleFilter();
        //noOfPages();
    }

    private void setDTO(List<User> u) {
        for(User user:u){
            UserFriendshipDTO uf=new UserFriendshipDTO(user.getFirstName(),user.getLastName(),user.getEmail(), LocalDateTime.now(),fsrv.getStatus(logged.getId(),user.getId()));
            if(uf.getStatus().isEmpty()||uf.getStatus().equals("Rejected")||uf.getStatus().equals("None"))
                uf.setStatus("Not friends");
            else {
                if (uf.getStatus().equals("Accepted"))
                    uf.setStatus("Friends");
                else
                    uf.setStatus("Request sent");
            }
            users.add(uf);
        }
    }

    public void handleNewMessage() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/NewMessageView.fxml"));
        AnchorPane layout = loader.load();
        primaryStage.setScene(new Scene(layout));
        primaryStage.setTitle("New message");
        NewMessageController controller = loader.getController();
        controller.setFields(logged,usrv,fsrv,msrv,esrv, primaryStage);
    }

    public void handleEvents() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/EventsView.fxml"));
        AnchorPane layout = loader.load();
        primaryStage.setScene(new Scene(layout));
        primaryStage.setTitle("Events");
        EventsController controller = loader.getController();
        controller.setFields(logged,usrv,fsrv,msrv,esrv, primaryStage);
    }
}
