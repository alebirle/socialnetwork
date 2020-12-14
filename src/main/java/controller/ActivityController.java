package controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import socialnetwork.domain.*;
import socialnetwork.service.EventService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActivityController {
    private UserService usrv;
    private FriendshipService fsrv;
    private MessageService msrv;
    private User logged;
    private Stage primaryStage;
    @FXML
    ListView listView;
    @FXML
    DatePicker datepickerFrom;
    @FXML
    DatePicker datepickerTo;
    ObservableList<Tuple<String, LocalDateTime>> activity= FXCollections.observableArrayList();
    private EventService esrv;

    @FXML
    public void initialize() {
        listView.setItems(activity);
        datepickerFrom.setFocusTraversable(false);
        datepickerTo.setFocusTraversable(false);
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

    public void handleShow() {
        if(datepickerFrom.getValue()==null||datepickerTo.getValue()==null){
            MessageAlert.showErrorMessage(null,"You must pick an interval!");
        }
        else{
            initActivity();
            LocalDate from=datepickerFrom.getValue();
            LocalDate to=datepickerTo.getValue();
            List<Tuple<String,LocalDateTime>> filter=new ArrayList<>();
            for(Tuple<String,LocalDateTime> a: activity){
                if(a.getRight().toLocalDate().isAfter(from)&&a.getRight().toLocalDate().isBefore(to))
                    filter.add(a);
            }
            activity.setAll(filter);
        }
        listView.scrollTo(activity.size()-1);
    }

    public void handlePDF() {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("activity.pdf"));
            document.open();
            PdfPTable table = new PdfPTable(1);
            Phrase p=new Phrase("Your activity");
            p.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            PdfPCell title=new PdfPCell(p);
            title.setBorder(Rectangle.BOTTOM);
            table.addCell(title);
            for(Tuple<String,LocalDateTime> a:activity) {
                PdfPCell cell=new PdfPCell(new Phrase(a.getLeft()+" | "+a.getRight()));
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
            }
            document.add(table);
            document.close();
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Saved!","Your messages were saved in a pdf");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setFields(UserService usrv, FriendshipService fsrv, MessageService msrv, EventService esrv, User logged, Stage primaryStage) {
        this.logged=logged;
        this.usrv=usrv;
        this.fsrv=fsrv;
        this.msrv=msrv;
        this.esrv=esrv;
        this.primaryStage=primaryStage;
        initActivity();
        listView.scrollTo(activity.size()-1);
        listView.setPlaceholder(new Label("You have no activity so far"));
    }

    private void initActivity() {
        activity.clear();
        for(Message m:msrv.getSome(logged,"from")){
            User to = usrv.getOne(m.getTo().get(0).getId());
            if (m.getReply() == null)
                activity.add(new Tuple<>("You sent a message to " + to.getFirstName() + " " + to.getLastName() + ": " + m.getMessage(), m.getDate()));
            else
                activity.add(new Tuple<>("You replied to " + to.getFirstName() + " " + to.getLastName() + "'s message '" + m.getReply().getMessage() + "' with: " + m.getMessage(), m.getDate()));
        }
        for(Message m:msrv.getSome(logged,"to")){
            User from=usrv.getOne(m.getFrom().getId());
            if (m.getReply() == null)
                activity.add(new Tuple<>("You received a message from " + from.getFirstName() + " " + from.getLastName() + ": " + m.getMessage(), m.getDate()));
            else
                activity.add(new Tuple<>(from.getFirstName() + " " + from.getLastName() + " replied to your message '" + m.getReply().getMessage() + "' with: " + m.getMessage(), m.getDate()));
        }
        for(Friendship f:fsrv.getSome(logged,"All")){
            if(f.getId().getLeft().equals(logged.getId())) {
                User u=usrv.getOne(f.getId().getRight());
                activity.add(new Tuple<>("You became friends with " +u.getFirstName()+" "+u.getLastName()+" ("+u.getEmail()+")",f.getDate()));
            }
            if(f.getId().getRight().equals(logged.getId())) {
                User u=usrv.getOne(f.getId().getLeft());
                activity.add(new Tuple<>("You became friends with " +u.getFirstName()+" "+u.getLastName()+" ("+u.getEmail()+")",f.getDate()));
            }
        }
        Comparator<Tuple<String,LocalDateTime>> byDate = new Comparator<Tuple<String,LocalDateTime>>() {
            public int compare(Tuple<String,LocalDateTime> m1, Tuple<String,LocalDateTime> m2) {
                return Integer.valueOf(m1.getRight().compareTo(m2.getRight()));
            }
        };
        Collections.sort(activity,byDate);
    }
}
